# AGENTS.md

## 构建与验证约定

- 禁止并行运行多个 Gradle 命令。
- 每次只能启动一个 `./gradlew ...` 进程，必须等它结束后再运行下一个。
- 如果 Gradle daemon、依赖下载、缓存或文件 I/O 出错，先停止当前排查并说明现象，不要继续叠加新的 Gradle 任务。
- 如果 Gradle 遇到文件锁、文件占用或疑似 Windows 侧 IDEA/Gradle 同时访问导致的问题，停止排查并说明现象，让用户在 Windows/IDEA 侧运行命令。

## Minecraft 源码查阅约定

- 需要查看 Minecraft 源码、字段、方法、内部类或游戏逻辑时，优先读取本仓库相邻目录 `../mc-source`。
- 只有 `../mc-source` 缺失、内容不足或需要核对构建产物时，才去查 Loom/Gradle cache、反编译输出或 jar。

## Mixin 与反射访问约定

- 26.1 开始生产环境不再把 Minecraft 类名、方法名、字段名混淆成 `class_1234`、`method_1234` 这类名字，因此反射、`MethodHandle`、`VarHandle` 可以作为可用手段。
- 访问 Minecraft 私有、包私有或受限成员时，仍应尽可能优先使用 Mixin accessor / invoker。
- 核心路径、热路径、长期稳定依赖的内部成员访问，默认使用 Mixin accessor / invoker。
- 低频探测、可选兼容、调试工具、允许优雅降级的访问，可以考虑集中封装 `MethodHandle` / `VarHandle`。
- 不要在业务代码里分散编写反射或 handle lookup；确实需要时应封装到明确的工具或兼容层中。

## GUI 兼容约定

- `ScreenRenderingContext` 的 `renderXxx(..., float z, ...)` 中，`z` 参数当前仅为兼容旧 API 保留，不要处理它。

## Platform 分层约定

- `platform` 包是底层兼容与抽象支撑层，不应该依赖 `platform` 外的业务、GUI、技能、网络 payload 等上层代码。
- 需要根据 Biology Dictionary 业务状态做分发或判断时，例如区分词典 screen 内消息和原版游戏 overlay，应放在 `BiologyDictionaryClient`、screen、manager 等上层入口里，不要下沉到 `platform` 工具类。

## 用户改动保护

- 如果发现文件内容与自己之前的改动不一致，禁止擅自改回去；这通常表示用户或其他流程已经修改过。应保留现状，必要时先询问这是否是用户有意改动。
