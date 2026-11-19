# AGENTS.md

通用开发守则见 `../AGENTS.md`（先读它，再读本文件）。

## 本目录特有规则

- 平台抽象：使用 `Platform.load(...)` 窄服务 + `@PlatformEntry` 静态定义；禁止引入 Architectury / `@ExpectPlatform`。
- 反射：禁止使用反射调用 MC 原版内容（测试代码除外），优先 Mixin；本模组、其他模组的数据可以反射。
- 映射：使用 Minecraft official mappings，不用 Yarn 命名。
- 资源类名：`ResourceLocation`。
- NeoForge 入口有参 `(IEventBus)`。
- 移植任务：见父目录 `.dsh/skills/port` skill。
