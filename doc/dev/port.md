# 跨版本移植指南

本文档为本模组在不同 MC 版本之间移植功能时提供通用策略、已知 API 差异和踩坑经验。

## 术语约定

- **源分支 (source)**：功能已经实现的高版本分支（1.21.1，NeoForge）
- **目标分支 (target)**：需要被移植到的低版本分支（1.20.1，Forge）
- **MC 源码**：位于 `../mc-source/<mc-version>/`，用于验证 API

## 移植策略

### 前提

- 两个分支的功能基线应当齐平（移植的 commit 之前功能一致）
- 不要只看 commit diff，diff 之外可能有隐含的依赖关系

### 方法选择

| 方法 | 适用场景 | 注意事项 |
|------|---------|---------|
| **git format-patch + git am** | 大部分文件（两个版本 MC 第一方逻辑差异较小） | 优先使用。冲突少时效率最高，冲突处手动解决 |
| **手动逐文件移植** | 冲突较多、渲染相关、API 差异大的文件 | 读取源分支的 diff，手动应用到目标分支 |
| **直接复制新文件并适配** | 全新文件 | 直接复制 + API 适配 |

### 流程

1. 分析 commit 改动范围（`git diff-tree --no-commit-id --name-only -r <commit>`）
2. 分类：新增文件 / 修改文件 / 删除文件
3. 优先尝试 `git format-patch -1 <commit>` + `git am`，处理冲突
4. 冲突无法自动解决或 API 差异较大的文件，手动将功能 diff 应用到目标分支上
5. **完整性检查**（见下方清单）
6. 编译验证

### 区分功能改动 vs MC 版本适配

对比源分支的基线和目标分支的同一文件：
- 差异已存在于基线中 → MC 版本差异，移植时保持目标分支的实现
- 差异仅出现在目标 commit 中 → 功能改动，需要移植

### 平台差异

| 方面 | 源分支 (1.21.1) | 目标分支 (1.20.1) |
|------|---------|--------|
| **Mod Loader** | NeoForge | Forge |
| **平台目录** | `neoforge/` | `forge/` |
| **构造函数** | `BiologyDictionaryNeoForge()` 无参 | `BiologyDictionaryNeoForge(IEventBus modBus)` 有参 |

移植时注意：源分支中 `neoforge/` 目录下的文件对应目标分支的 `forge/` 目录。

### 移植纪律

- **禁止自作主张调整移植代码**：不要对移植内容做代码格式化、翻译修改、或忽略细微差异。若无冲突应原封不动地移植。仅当存在冲突或方案替换时才调整代码，且应尽可能减小修改面
- **先问再做**：如果某个 API 在目标版本不存在或有显著差异，不要猜测，留下 `// TODO` 注释并询问
- **优先查 MC 源码**：在目标版本的 MC 源码中搜索对应类/方法
- **渲染部分特别小心**：`ScreenRenderingContext`、Mixin、渲染管线是重灾区

---

## 完整性检查清单

移植完成后，**必须**执行以下检查，不要只验证 commit diff 覆盖的文件：

1. **被移动/删除的方法**：`grep -rn "OldClass.methodName" --include="*.java" .` — 方法从类 A 移到类 B 时，diff 外的文件可能也在调用
2. **被修改签名的公共方法**：`grep -rn "\.methodName(" --include="*.java" .` — 添加/删除参数后，所有调用者都需要更新
3. **被删除的类**：`grep -rn "DeletedClassName" --include="*.java" .`
4. **`@ExpectPlatform` 配对**：确认 fabric/ 和 forge/ 都有对应实现
5. **import 残留**：确认没有 import 已删除/重命名的类
6. **neoforge → forge 路径**：确认没有残留对 `neoforge/` 包或 NeoForge 特有 API 的引用
7. **static → instance 转换**：区分「类型引用」和「方法调用」——内部类类型引用（如 `HighlightManager.HighlightedBlock`）不需要改，只有静态方法调用需要改

> **不要依赖编译来发现遗漏**：编译能发现语法错误，但无法发现语义上的遗漏。完整性检查应该在编译之前就做。

---

## 已知的 API 差异（1.21.1 vs 1.20.1）

> 1.21.1 和 1.20.1 的 MC 第一方逻辑大部分相同，差异较少。

### 10. Forge 入口

| 1.21.1 (NeoForge) | 1.20.1 (Forge) |
|---------|--------|
| 无参构造 `BiologyDictionaryNeoForge()` | 有参构造 `BiologyDictionaryNeoForge(IEventBus modBus)` |
| `neoforge/` 平台目录 | `forge/` 平台目录 |
