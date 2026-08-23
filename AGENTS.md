# AGENTS.md

通用开发守则见 `../AGENTS.md`（先读它，再读本文件）。

## 本目录特有规则

- 平台抽象：使用 `Platform.load(...)` 窄服务 + `@PlatformEntry` 静态定义；禁止引入 Architectury / `@ExpectPlatform`。
- 反射：26.1 起生产环境不再混淆 MC 类名/方法名/字段名，允许反射处理 MC 内容（但仍 Mixin 优先；访问私有成员优先 Mixin accessor / invoker）。
- 映射：使用 Minecraft official mappings，不用 Yarn 命名。
- 资源类名：`Identifier`。
- 移植任务：见父目录 `.dsh/skills/port` skill。
