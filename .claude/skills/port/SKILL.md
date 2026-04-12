---
name: port
description: 跨版本移植助手，将功能 commit 从源分支移植到当前分支
---

# /port - 跨版本移植助手

将功能 commit 从源分支移植到当前分支。

## 参数

- `$ARGUMENTS`：`<commit> [source-branch]`
  - `<commit>`：要移植的 commit hash（源分支上的）
  - `<source-branch>`：源分支名（默认 `origin/main-architectury`）

示例：`/port 3bfb220 origin/main-architectury`

多个连续 commit 可以一次性给出：`/port 5ec3f1c 2fecafd f4cf685 origin/main-architectury`

---

## 分工原则

| 适合命令 / 工具 | 适合 AI |
|---------|--------|
| `git show` 提取文件内容 | 理解 diff 意图 |
| `git diff-tree` 获取文件列表 | 判断「功能改动」vs「MC 版本适配」 |
| grep 搜索、sed 机械替换 | 复杂 API 适配方案的选择 |
| 编译 | 理解上下文并做出合理的代码改写 |
| | 渲染逻辑的理解和等价实现 |

---

## 流程

严格按顺序执行。每完成一个 phase 再进入下一个。

### Phase 0：分析

1. 解析参数。只有一个值时作为 commit，source branch 默认 `origin/main-architectury`
2. `git diff-tree --no-commit-id --name-only -r <commit>` 获取文件列表
3. 分类为：**新增 / 修改 / 删除**
4. 对修改文件，`git show <commit> -- <file>` 逐个看 diff，判断每处改动是「功能改动」还是「MC 版本适配」。只标记需要移植的功能改动。
5. 输出摘要表格：
   - 新增文件数量
   - 需要移植的修改文件列表 + 每个文件要改什么
   - 删除文件列表
   - 需要跳过或留 TODO 的部分

**这一步只看 diff，不做任何修改。**

### Phase 1：新增文件 + 手动移植

> **教训：`git format-patch + git am` 对涉及大量文件的 commit 效果很差**（实测 ~78 文件的 commit 产生 ~15 个冲突，最终全部 abort 改为手动移植）。
> **仅在 commit 修改文件数 ≤ 10 时尝试 git am**，否则直接手动移植。

#### 1.1 新增文件

对每个新增文件，`git show <commit>:<path>` 提取内容并写入目标分支。

#### 1.2 修改文件

对每个需要移植的修改文件：
1. `git show <commit> -- <file>` 拿 diff
2. 读目标文件中需要修改的区域
3. AI 理解 diff 意图后，在目标文件上应用等价的功能改动

#### 1.3 平台路径映射

源分支中 `neoforge/` 目录下的文件对应目标分支的 `forge/` 目录。
新文件可能是 neoforge 专有实现，需要编写对应的 forge 实现。

#### 1.4 API 适配

已知 API 差异参考 `doc/dev/port.md`。未在已知差异表中出现的 API：在 `../mc-source/1.20.1/` 中搜索替代方案。

> **教训：不要对 mod 自定义类留 TODO**。只有 MC 自身的 API 差异才需要 TODO。
> 例如 `DiscoveryRecord` 是 mod 自己的类，两个版本都有；`ServerboundClientCommandPacket` 在 1.20.1 中也存在。
> 不确定时先在 `../mc-source/<version>/` 中搜索确认，再在目标分支项目中 grep 搜索确认。

按依赖顺序从底层到上层（核心类 → session → 网络 → 技能/组件 → GUI → Mixin）。

> **大 commit 可以拆分为并行子任务**。按文件模块分组（core、net/session、GUI/screen、skill/mixin），
> 使用 Task tool 并行处理。但要注意：**不同 agent 修改同一文件可能冲突**，
> 后运行的 agent 的 Edit 可能覆盖先运行 agent 的改动。Phase 3 完整性检查可以捕获这类问题。

### Phase 2：git format-patch + git am（仅小 commit）

1. `git format-patch -1 <commit> --stdout > /tmp/port.patch` 导出 patch
2. `git am /tmp/port.patch` 尝试直接应用
3. 如果成功且无冲突，跳到 Phase 3
4. 如果有冲突，解决冲突文件后 `git am --continue`。无法解决的才 `git am --abort` 留给手动移植

### Phase 3：完整性检查

用 grep 全局扫描，**不要读文件**。以下清单必须全部执行：

```bash
# 1. 检查被删除的类是否仍被引用
grep -rn "DeletedClassName" --include="*.java" .

# 2. 检查 @ExpectPlatform 方法是否在两端都有实现
grep -rn "@ExpectPlatform" common/ --include="*.java"
# 对每个 @ExpectPlatform 方法，确认 fabric/ 和 forge/ 都有对应实现

# 3. 检查残留的 TODO（本次移植留下的）
grep -rn "TODO.*adapt\|TODO.*1.21.1\|TODO.*1.20.1" --include="*.java" .

# 4. 检查 Java 版本不兼容的 API
grep -rn "\.addFirst(" --include="*.java" .           # Java 21 only
grep -rn "ByteBufferBuilder" --include="*.java" .     # 1.21.1 only

# 5. 检查残留的旧 API 调用
grep -rn "ConfigsManager\.broadcast" --include="*.java" .  # 应已替换为 onUpdated

# 6. 检查 forge/ 中是否有 neoforge 引用
grep -rn "neoforge\|net\.neoforged" forge/ --include="*.java"

# 7. 检查已知的 1.21.1 专有 API
grep -rn "lookupOrThrow\|LookupOrThrow" --include="*.java" .
grep -rn "EntitySpawnReason" --include="*.java" .
grep -rn "import.*Identifier\b" --include="*.java" .
```

发现问题立即修复，不要等到编译才发现。

> **教训：Phase 3 应该额外验证 agent 留的 TODO 是否合理**。
> 有些 agent 会对实际存在于两个版本的 mod 自定义类留 TODO（因为类名或 API 名暗示了 MC 版本差异），
> 但实际上 mod 自定义类在两个分支间是一致的。这类 TODO 是误判，应该直接实现而不是留 TODO。

### Phase 4：编译验证

运行 `./gradlew build` 编译。修复编译错误后重新编译直到通过。

> **编译通过 ≠ 移植正确**。编译只能发现语法和类型错误，无法发现语义遗漏。
> Phase 3 的完整性检查比编译更重要。

---

## 注意事项

- **不要迷信 git patch**：对于涉及新文件 + 平台映射的大型 commit，手动移植比 git am 更可控
- **不要对 mod 自定义类留 TODO**：mod 自己的类（如 DiscoveryRecord、ConfigsManager 方法）在两个分支间是一致的。只有 MC 原生 API 才需要适配
- **Java 版本差异**：1.21.1 运行在 Java 21 上，1.20.1 运行在 Java 17 上。`List.addFirst()`、`SequencedCollection` 等 Java 21 API 在 1.20.1 中不可用
- **渲染相关**：ScreenRenderingContext、Mixin、实体渲染是重灾区，不确定的留 TODO 问用户
- **VertexConsumer**：1.20.1 的 VertexConsumer 接口有 `defaultColor()` 和 `unsetDefaultColor()` 抽象方法，实现 NOOP 消费者时必须 override
- **不要猜测 API**：不确定目标版本有没有某个类/方法时，先在 `../mc-source/1.20.1/` 中搜索确认，再在目标分支项目中搜索确认
- **保持目标分支风格**：遵守 CLAUDE.md 编码规则，不引入源分支的风格差异
- **禁止自作主张调整移植代码**：不要对移植内容做代码格式化、翻译修改、或忽略细微差异。若无冲突应原封不动地移植。仅当存在冲突或方案替换时才调整代码，且应尽可能减小修改面
- **及时沟通**：遇到以下情况停下来问用户：
  - 渲染逻辑找不到等价实现
  - 某个功能在目标版本可能需要完全不同的实现方式
  - 不确定某个改动是功能性的还是 MC 适配性的
