---
name: port
description: 跨版本移植助手，将功能 commit 从源分支移植到当前分支
---

# /port - 跨版本移植助手

参数：`$ARGUMENTS` = `<commit> [source-branch]`（source-branch 默认 `origin/main-architectury`）

## ⚠️ 移植纪律（最高优先级，每次移植都必须遵守）

1. **原封不动移植**：每一行、每个词、每条注释、每个空行都必须与原补丁一致。不要 reformat、不要删注释、不要改命名风格。仅当 API 差异导致编译错误时才调整，且修改面尽可能小。
2. **旧引用必须更新**：测试代码中的字符串常量、注释中的旧类名等引用也要同步更新。
3. **移植后逐文件 diff 审计**：对每个移植文件 `git show <commit>:<path>` vs 本地文件，确认每个差异都有 API 适配理由。**不能跳过。**

## 流程

严格按顺序执行。

### Phase 0：分析
`git diff-tree --no-commit-id --name-only -r <commit>` 获取文件列表，分类为新增/修改/删除（含 `.java` 和 `.md` 等非代码文件）。对修改文件逐个看 diff，区分功能改动 vs MC 版本适配。只标记功能改动。**只看不做。**

### Phase 1：新增文件
`git show <commit>:<path>` 提取内容写入目标分支。已知 API 差异参考 `docs/dev/port.md` 批量替换，未知差异在 `../mc-source/<版本>/` 搜索。无法适配留 `// TODO`。

### Phase 2：修改文件
按依赖顺序从底层到上层（核心类 → session → 网络 → 技能/组件 → GUI → Mixin）。拿 diff，理解意图，在目标文件应用等价改动。

### Phase 3：完整性检查
grep 全局扫描：被移动/删除的方法、被修改签名的公共方法、被删除的类、`@ExpectPlatform` 配对、import 残留、markdown/changelog 变更。发现问题立即修复。

### Phase 4：编译验证
提醒用户编译，根据报错修复。

## 参考
- 详细 API 差异表：`docs/dev/port.md`
- 编码规范：`CLAUDE.md`
