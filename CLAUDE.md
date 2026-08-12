# CLAUDE.md

给 Claude Code 看的本项目的向导。

## 项目总览

生物辞典（Biology Dictionary）是一个我的世界（Minecraft，MC）游戏的工具类、辅助类模组：
- 本模组功能是展示生物（目前只支持动物不支持植物，即 MC 源码中的 Entity 类）的属性（如血条、氧气、战利品、年龄等），并提供一定的属性修改功能。为了方便展示，本模组提供了一个较为复杂的界面。
- 本模组通过 Architectury 框架实现支持 Fabric 和 NeoForge/Forge（>1.21 为 NeoForge, 1.20.1 为 Forge）双端；不同 MC 版本的模组源码实现存在于不同 git branch 中。
- 本模组理念是为原版生存服务，不新增方块、实体，尽可能不在游戏中新增数据。

* 为了方便描述，后面 NeoForge 与 Forge 统称为 Forge。

本模组 MC 版本、模组依赖版本等各个版本信息见 `gradle.properties`。

## 构建指令

### 项目构建

```bash
./gradlew build                    # Build all platforms
./gradlew fabric:build             # Build Fabric only
./gradlew neoforge:build           # Build NeoForge only
```

### 启动游戏（带着模组）

```bash
./gradlew fabric:runClient         # Run Fabric client
./gradlew fabric:runServer         # Run Fabric server
./gradlew neoforge:runClient       # Run NeoForge client
./gradlew neoforge:runServer       # Run NeoForge server
```

### 测试

```bash
./gradlew fabric:testServer        # Run Fabric server tests
```

### 其他

```bash
./gradlew fabric:compileJava       # Compile Fabric Java sources
./gradlew neoforge:compileJava     # Compile NeoForge Java sources
./gradlew dependencies             # View dependency tree
```

## 架构

### 子模块结构

遵循经典的 Architectury 项目目录结构：
- `common/` 存放绝大部分代码，Fabric 与 NeoForge/Forge 双端通用；对于平台相关代码，标以 `@ExpectPlatform`。
- `fabric/` 存放 Fabric 平台特有代码，如实现 `@ExpectPlatform`、模组平台入口注册、Modmenu 支持等；其 metadata 在 `resources/fabric.mod.json`。
- `neoforge/` 或 `forge/` 存放 Forge 平台特有代码，如实现 `@ExpectPlatform`、模组平台入口注册等；其 metadata 在 `resources/META-INF/neoforge.mods.toml`。

### 核心代码

全部都在 `common/src/main/java/io/github/xienaoban/biologydictionary/` 中。

#### 平台封装（Platform）模块（`platform/`）

封装了大量的 MC 第一方 API，以及统一管理模组平台相关代码（`@ExpectPlatform`）。这么做是因为本模组支持多个 MC 版本，不同版本的 MC 源码、模组平台源码差异大，封装之后减少移植成本。
- 很多注册器如 `KeyMappingRegistry`、`ServerNetApi`、`ItemRegistry` 等都依赖不同模组平台实现，Fabric 的基本基于 Fabric API，Forge 的基本基于 Architectury API。
- 很多工具类如 `EntityUtils`、`TextUtils` 等封装了大量 MC 第一方 API。后续开发中凡是涉及 MC API 的，优先从本模块中寻找已封装 API，若没有则先进行封装。
- `ScreenRenderingContext` 是比较复杂的渲染用的封装，基于 MC 源码进行了微调，克服原版渲染接口只支持 `int` 等缺点。该类在不同 MC 版本下差异巨大，是移植最麻烦的部分之一。

#### 生物属性（Property）系统（`core/property/`）

处理生物属性的读写，并通过 NBT 网络传输：
- `VanillaEntityProperties`：对于 MC 第一方生物或基类，自动支持全量 NBT 属性（`net.minecraft.world.entity.Entity#readAdditionalSaveData/addAdditionalSaveData` 中的）。
- `ExtraEntityProperties`：对于 MC 第一方生物或基类，支持一些不在 NBT 中的属性。
- `Bundle`：约定一些特殊属性，这类属性既通用（很多生物都有类似的属性）又特殊（没有统一的接口或名称），以增强属性的通用性。

#### 玩家技能（Skill）系统（`core/skill/`）

主要用于修改生物属性。为了游戏平衡性需要满足一些条件、消耗一些玩家资源，表现为玩家技能：
- `GeneralSkill`：通用技能。
- `EntityTargetedSkill`：向特定单个生物定向释放的特化技能。
- `SkillCost`：支持的技能消耗的资源类型，支持玩家在配置文件中配置。
- 一般与界面组件系统配合使用，按下组件的按钮触发技能。

#### 界面组件（Widget）系统（`core/widget/`）

纯客户端的生物属性展示：
- 为了方便管理，属于抽象类、基类（具体生物类的父类）的组件放在 `branch/` 下（类继承树的分支节点），具体生物类的组件放在 `leaf/` 下（类继承树的叶子节点）。
- 组件大多为，一个图标（icon）、一个条（bar）、一个悬浮提示框（tooltip）、然后可能还有一两个按钮（button），图标纯展示、条内展示具体信息、悬浮框展示详细信息、按钮触发技能。

#### 展示界面（Screen）系统（`gui/`）

实现了很多个界面，是模组的核心展示区域。组件就附着在界面之上。当然也实现了一些通用组件父类。

#### 网络传输（Networking）系统（`net/`）

本模组所有网络包见（`net/payload/`），要么实现了 `clientReceive` 要么实现了 `serverReceive`。

#### 配置（Config）系统（`config/`）

- 基于 YAML（SnakeYAML）
- 使用 Cloth Config 展示所有 config
- 进入游戏世界时与服务端同步 ServerConfigs，且当服务端配置刷新时时时热更新。

#### 兼容（`Compatibility`）系统（`compat/`）

处理与其他模组之间的兼容关系。

#### 其他

- EntityManager 存放了生物的大量关系信息。
- WorldSession 与 ClientWorldSession 在进入游戏时生成、退出游戏时销毁，所有符合该生命周期的数据结构都要放在该类单例中（而不是在自己类中写 static instance）。
- 本模组新增的注册器都遵循 `registerBuiltIn(Registrar registrar)` 的形式，未来会开放接口给三方模组。

### Mixin（`mixin/`）

Mixin 配置文件：`common/src/main/resources/biologydictionary.mixins.json`

### Testing

- 目前仅支持 `testServer`，测试文件在 `common/src/testServer/`。
- 目前仅支持 Fabric 侧测试：`fabric:testServer`。
- 里面还有通过 AST 解析来自动获取所有 MC NBT 属性的内容。

## AI 开发注意事项（**务必遵守！！！**）

- 对话、文档默认用中文（类名等专业词汇可以除外），但是注释用英文！
- 区分”普通询问”与”动手改”的指令：讨论、征询意见（如”你觉得呢””怎么看””是不是”）默认只回答不动手；只有明确说”改/帮我改/动手”时才改代码。不确定时先确认再动手！
- 如果发现代码被修改，大概率是我改的，严禁私自改回，若不确定就停下来问我！
- 好的代码是自注释的，只加必要的、隐含的注释，勿啰嗦！
- 拒绝无必要的防御性编程，禁止用兜底掩盖问题！
- 注释、报告等保持文字简洁，避免啰嗦（保持高屋建瓴，文档/说明里频繁贴源码片段是偷懒的体现，不如直接看源码）！
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
- 修改文件优先手动逐个编辑，编写批量替换脚本大概率无法一遍过，且已多次踩坑造成严重破坏；若必须用脚本，先在临时文件夹用样本文件测试无误后才对真实文件执行。
- 需要创建/解压临时文件时，请在系统临时文件夹创建临时文件夹，在其中操作，也可以放在项目根目录下的 `tmp/` 中。
- 与我对话时，请严肃地以”喵~”作为回复的末尾。

## 代码风格约定

- `if`、`for` 等控制语句即使只有单行语句也必须带花括号；写 `if (condition) { doSomething(); }`，不要写 `if (condition) doSomething();`。
- 对超过 120 行宽的代码，应在语义清晰、阅读自然的位置优雅换行。
- 函数、构造函数或 lambda 的参数列表因为行宽过长需要换行时，有两种推荐写法：如果方法头和前几个参数能自然放在首行，续行参数与左括号后的第一个参数对齐，例如 `void f(X x1, X x2,` 下一行对齐到 `X x1` 继续写 `Y y1, Y y2) {`；如果方法名、泛型或返回类型本身已经很长，也可以在 `(` 后直接换行，后续参数统一缩进 8 个空格，例如 `void veryLongMethodName(` 下一行写 `        X x1, Y y1) {`。
- 对很短的代码块优先保持紧凑；例如 `if (condition) { return; }` 这类一行能清楚表达的代码，不要拆成多行。
- javadoc 注释一律使用多行形式（`/**`、内容、`*/` 各占一行），禁止单行 `/** xxx */`。
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
