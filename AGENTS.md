# AGENTS.md

通用开发守则见 `../AGENTS.md`（先读它，再读本文件）。

## 本目录特有规则

- 平台抽象：Architectury，`common` 用 `@ExpectPlatform` 标记平台相关代码，`fabric/` 与 `neoforge/` 分别实现。
- 反射：禁止使用反射调用 MC 原版内容（测试代码除外），优先 Mixin；本模组、其他模组的数据可以反射。
- 资源类名：`Identifier`（1.21.11 起 `ResourceLocation` 改名）。
- 移植任务：见父目录 `.dsh/skills/port` skill。
