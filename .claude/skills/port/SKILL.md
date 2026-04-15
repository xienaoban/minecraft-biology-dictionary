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

---

## 分工原则

| 适合命令 | 适合 AI |
|---------|--------|
| 提取文件列表、复制文件 | 理解 diff 意图 |
| grep 搜索、sed 机械替换 | 判断「功能改动」vs「MC 版本适配」 |
| 编译 | 复杂 API 适配方案的选择 |
| | 理解上下文并做出合理的代码改写 |
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

### Phase 1：新增文件

1. 对每个新增文件，`git show <commit>:<path>` 提取内容并写入目标分支
2. 已知 API 差异用 sed 批量替换（参考 doc/dev/port.md）：
   - `Identifier` ↔ `ResourceLocation`（import + 全文）
   - NBT optional API ↔ 直接返回 API
   - `lookupOrThrow` ↔ `registryOrThrow`
   - `EntitySpawnReason` ↔ `MobSpawnType`
   - 等等
3. 未在已知差异表中出现的 API：在 `../mc-source/1.21.1/` 中搜索替代方案，由 AI 判断如何适配
4. 无法适配的留 `// TODO: adapt for 1.21.1`，在输出中提醒用户

### Phase 2：修改文件

按依赖顺序从底层到上层（核心类 → session → 网络 → 技能/组件 → GUI → Mixin）。

对每个修改文件：
1. `git show <commit> -- <file>` 拿 diff
2. 读目标文件中需要修改的区域
3. AI 理解 diff 意图后，在目标文件上应用等价的功能改动


### Phase 3：完整性检查

用 grep 全局扫描，**不要读文件**：

```bash
# 根据实际移植情况替换下面的搜索词
grep -rn "被移动的方法" --include="*.java" .
grep -rn "被删除的类" --include="*.java" .
grep -rn "@ExpectPlatform" common/ --include="*.java"
# 检查是否 import 了已删除/重命名的类
```

发现问题立即修复，不要等到编译才发现。

### Phase 4：编译验证

提醒用户编译。用户提供报错后修复。

---

## 注意事项

- **渲染相关**：ScreenRenderingContext、Mixin、实体渲染是重灾区，不确定的留 TODO 问用户
- **不要猜测 API**：不确定目标版本有没有某个类/方法时，先在 `../mc-source/1.21.1/` 中搜索确认
- **保持目标分支风格**：遵守 CLAUDE.md 编码规则，不引入源分支的风格差异
- **禁止自作主张调整移植代码**：不要对移植内容做代码格式化、翻译修改、或忽略细微差异。若无冲突应原封不动地移植。仅当存在冲突或方案替换时才调整代码，且应尽可能减小修改面
- **Mixin Accessor 转型**：编译时目标类未实现 Mixin 接口，必须 `((MixinType) (Object) target)` 中间转型
- **NBT `getList` 类型参数**：1.21.1 会按 type 过滤元素，写入 `StringTag` 必须用 `Tag.TAG_STRING` 读取，用错类型会静默返回空列表
- **及时沟通**：遇到以下情况停下来问用户：
  - 渲染逻辑找不到等价实现
  - 某个功能在目标版本可能需要完全不同的实现方式
  - 不确定某个改动是功能性的还是 MC 适配性的
