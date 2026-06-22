# AGENTS.md

## AI 开发注意事项（**务必遵守！！！**）

- 对话用中文（类名等专业词汇可以除外），但是注释用英文！
- 好的代码是自注释的，啰嗦的注释不要加，只加必要的！
- 除非出现重名等情况，避免使用类的全限定名，而是使用 `import` + simple name 的方式（字符串中除外）！
- 如非必要，勿改现有无关代码，不是不允许重构，但重构前先告知并经过我同意！
- 编码时注意当前的 MC 版本（见 `gradle.properties` 的 `minecraft_version=?`），不同版本差异大！
- MC 第一方代码我通常解压并放在了 `<本项目根目录>/../mc-source/<MC 版本>` 下，可以参考！
- `../mc-source` 缺失、内容不足或需要核对构建产物时，停下来询问我，我确认确实没有后才去查 Loom/Gradle cache、反编译输出或 jar。
- 优先查阅本地的 MC 官方第一方代码，而非在网络上搜索！
- 若必须网络检索 MC 相关内容，务必校验所参考内容与目标 MC 版本的兼容性。MC 不同版本间源码差异极大！
- 在 1.21.11 及以上，老版本的 `ResourceLocation` 改名为了 `Identifier`，要注意！
- 禁止使用 `org.jetbrains.annotations.Nullable` 和 `org.jetbrains.annotations.NotNull` 注解。
- 如果发现文件内容与自己之前的改动不一致，禁止擅自改回去；这通常表示我基于你的代码新修改过，应保留现状，必要时先询问这是否是用户有意改动。
- 与我对话时，请严肃地以“喵呜~”作为回复的末尾。

## 代码风格约定

- `if`、`for` 等控制语句即使只有单行语句也必须带花括号；写 `if (condition) { doSomething(); }`，不要写 `if (condition) doSomething();`。
- 对超过 120 行宽的代码，应在语义清晰、阅读自然的位置优雅换行。
- 函数、构造函数或 lambda 的参数列表因为行宽过长需要换行时，有两种推荐写法：如果方法头和前几个参数能自然放在首行，续行参数与左括号后的第一个参数对齐，例如 `void f(X x1, X x2,` 下一行对齐到 `X x1` 继续写 `Y y1, Y y2) {`；如果方法名、泛型或返回类型本身已经很长，也可以在 `(` 后直接换行，后续参数统一缩进 8 个空格，例如 `void veryLongMethodName(` 下一行写 `        X x1, Y y1) {`。
- 对很短的代码块优先保持紧凑；例如 `if (condition) { return; }` 这类一行能清楚表达的代码，不要拆成多行。
- 对大段后续逻辑，优先使用 guard clause：写 `if (condition) { return; }` 后接后续逻辑，不要写成 `if (condition) { return; } else { ... }`。如果后续逻辑只有一两句，或存在多段 `else if` 分支，则可按可读性保留 `else` / `else if`。
- 业务代码中极力避免使用全限定类名，优先使用 `import` 类名；不推荐 `import static`，`BiologyDictionary.LOGGER` 除外。
- 注释中的引用，例如 `@see`，对于 Minecraft、Mojang、Fabric 等官方类应尽量使用全限定类名，方便未来移植到新版本时定位函数名；本模组类避免全限定。
- 构造函数里尽量用 `this.` 访问成员，除此之外尽可能避免使用 `this.`。
- 纯工具类加上 `private` 构造函数。
- 类里的第一个函数/成员的定义的行，不要与类定义的那行中间空一行。
- 有时我会在行中间多加几个空格以保持上下行的对齐；但是行末的空白空格是不允许的。
- 对于短小的代码，有时候为了视觉精简，将注解写在类/函数/变量定义的同一行是合理的。

## 构建与验证约定

- 禁止并行运行多个 Gradle 命令。
- 每次只能启动一个 `./gradlew ...` 进程，必须等它结束后再运行下一个。
- 如果 Gradle daemon、依赖下载、缓存或文件 I/O 出错，先停止当前排查并说明现象，不要继续叠加新的 Gradle 任务。
- 如果 Gradle 遇到文件锁、文件占用或疑似 Windows 侧 IDEA/Gradle 同时访问导致的问题，停止排查并说明现象，让用户在 Windows/IDEA 侧运行命令。

## Mixin 与反射访问约定

- 1.21.11 及之前禁止使用反射调用 MC 原版内容（测试代码除外），因为 MC 生产运行环境会对字段和方法名进行混淆处理。优先使用 Mixin 替代。26.1 之后允许使用反射，但仍然 Mixin 优先。
- 26.1 开始生产环境不再把 Minecraft 类名、方法名、字段名混淆成 `class_1234`、`method_1234` 这类名字，因此反射、`MethodHandle`、`VarHandle` 可以作为可用手段。
- 访问 Minecraft 私有、包私有或受限成员时，仍应尽可能优先使用 Mixin accessor / invoker。
- 核心路径、热路径、长期稳定依赖的内部成员访问，默认使用 Mixin accessor / invoker。
- 低频探测、可选兼容、调试工具、允许优雅降级的访问，可以考虑集中封装 `MethodHandle` / `VarHandle`。
- 不要在业务代码里分散编写反射或 handle lookup；确实需要时应封装到明确的工具或兼容层中。

## Platform 分层约定

- `platform` 包是底层兼容与抽象支撑层，不应该依赖 `platform` 外的业务、GUI、技能、网络 payload 等上层代码。
- 需要根据 Biology Dictionary 业务状态做分发或判断时，例如区分词典 screen 内消息和原版游戏 overlay，应放在 `BiologyDictionaryClient`、screen、manager 等上层入口里，不要下沉到 `platform` 工具类。

## GUI 兼容约定

- `ScreenRenderingContext` 的 `renderXxx(..., float z, ...)` 中，`z` 参数当前仅为兼容旧 API 保留，不要处理它。
