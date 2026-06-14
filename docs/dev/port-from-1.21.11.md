# Port From 1.21.11

本文档记录从 git branch `main-architectury-1.21.11` 迁移到 `main-26.1.2` 时的架构约束、迁移策略和已形成的样板。

## 目标仓库

当前仓库目标版本见 `gradle.properties`：

- Minecraft: `26.1.2`
- Java: `25`

当前仓库不是 Architectury 项目，也不要改造成 Architectury 项目。目录结构是手写 multi-platform：

- `common/`：跨平台通用代码与资源。
- `fabric/`：Fabric 平台代码、资源与构建配置，尽量保留 Fabric 官方模板习惯。
- `neoforge/`：NeoForge 平台代码、资源与构建配置，尽量保留 NeoForge 官方模板习惯。

参考项目路径：

```text
../minecraft-biology-dictionary-architectury-1.21.11
```

参考项目是功能来源，不是架构模板。

## 硬性约束

- 禁止引入 Architectury 框架、Architectury API、`architectury-plugin`、`dev.architectury.loom`、`@ExpectPlatform`。
- Fabric 侧使用 Fabric Loader、Fabric API、Fabric Loom。
- NeoForge 侧使用 NeoForge ModDevGradle。
- 从 MC 26.1 开始没有 Yarn mappings，本项目只使用 Minecraft official mappings。
- `common/src/main/java` 不得直接导入 Fabric、Forge、NeoForge 平台包；根项目已有 `checkCommonPlatformImports` 检查。
- 不要把平台侧生命周期、event bus、registration API 泄漏到 common。
- 禁止用反射调用 MC 原版私有或受限内容；需要访问时优先使用 Mixin accessor/invoker。
- 禁止使用 `org.jetbrains.annotations.Nullable` 和 `org.jetbrains.annotations.NotNull`。

## 总体迁移原则

先读当前 26.1.2 仓库，再读 1.21.11 参考仓库，不要凭记忆迁移 MC/API 代码。

common 负责：

- 业务模型与纯 Minecraft API 逻辑。
- packet 类型、codec、方向与业务 handler 定义。
- key mapping、creative tab entry、command、event listener 等静态注册清单。
- 需要平台消费的入口或字段，使用 `@PlatformEntry` 标注。

platform 负责：

- 在 Fabric / NeoForge 各自正确生命周期中消费 common 定义。
- 选择正确 Fabric API、NeoForge mod event bus 或 `NeoForge.EVENT_BUS`。
- 保存平台工具链的官方模板风格。

运行时平台能力使用窄服务：

- common 定义小接口。
- 通过 `Platform.load(...)` 获取平台实现。
- Fabric/NeoForge 分别提供 `Impl`。

不要在 common 中写一个总 `init()` 主动按固定顺序注册所有平台内容。不同平台注册阶段可能不同，common 固定顺序会迫使平台层补全局状态、延迟队列、初始化 flag，最终接近重新实现一个小 Architectury。

## 三个典型样板

### KeyMappings

适用于 common 定义静态入口、平台侧注册。

- common 文件：`common/src/main/java/io/github/xienaoban/biologydictionary/client/KeyMappings.java`
- common 中定义 `KeyMapping` 字段。
- 每个需要平台注册的字段标 `@PlatformEntry`。
- Fabric client 入口用 Fabric API 注册。
- NeoForge client 入口在 `RegisterKeyMappingsEvent` 中注册。

这种模式适合 key mappings、creative tab entries、部分 renderer/model/screen/provider 静态定义。

### DevUtils

适用于运行时平台能力查询或动作。

- common 文件：`common/src/main/java/io/github/xienaoban/biologydictionary/platform/util/DevUtils.java`
- common 定义窄 `PlatformBridge`。
- common 通过 `Platform.load(PlatformBridge.class)` 调用平台实现。
- Fabric/NeoForge 分别实现 `DevUtilsImpl`。

这种模式适合：

- `isModLoaded`
- `getModVersion`
- `getConfigDir`
- 网络发送 API
- 少量运行时 client/server 工具动作

不适合注册阶段强绑定的内容。

### ClientEventRegistry -> ClientEvents

适用于旧 Architectury register facade 的迁移。

- 旧类 `common/.../platform/client/ClientEventRegistry.java` 保留为空壳。
- 空壳只写迁移提示和 `TODO: delete after port`，方便后续从旧代码搜索定位。
- 真实定义放到 `common/src/main/java/io/github/xienaoban/biologydictionary/client/ClientEvents.java`。
- `ClientEvents` 中使用静态 `List.of(...)` 保存 listener 清单。
- 每个需要平台消费的 list 标 `@PlatformEntry`。
- Fabric / NeoForge client 入口分别读取这些 list，并注册到各自平台事件系统。

同类迁移方式也适用于：

- `ServerEventRegistry` -> `server/ServerEvents`
- `CommandRegistry` -> `server/Commands`

## 注册策略

### Common 定义，平台注册

优先用于注册型内容：

- key mappings
- network payload definitions
- creative tab entries
- commands
- client/server event listeners
- renderer/model/screen/provider registrations
- resource reload listeners
- 后续 item、data component、criterion 等 registry 清单

规则：

- common 中不直接调用 Fabric/NeoForge 注册 API。
- common 不规定平台实际注册时机。
- 平台入口在正确生命周期中逐项消费 common 定义。
- 需要平台消费的 common 字段或方法标 `@PlatformEntry`。

### Platform.load 窄服务

优先用于运行时动作或查询：

- `ClientNetApi.sendToServer(...)`
- `ServerNetApi.sendToPlayer(...)`
- `DevUtils.getModVersion(...)`
- `DevUtils.getConfigDir(...)`
- 后续打开平台相关 screen、检测 optional dependency 等即时动作

不要把强依赖 event object、registrar、mod event bus 的注册逻辑塞进 `Platform.load` facade。

## 平台入口职责

建议固定入口边界：

- `BiologyDictionary`：双端通用初始化，不引用 client-only 类。
- `BiologyDictionaryClient`：客户端通用初始化，只由平台 client 入口调用。
- `FabricBiologyDictionary`：Fabric common/server 注册与初始化。
- `FabricBiologyDictionaryClient`：Fabric client 注册与初始化。
- `NeoForgeBiologyDictionary`：NeoForge common/server 注册与初始化，区分 mod event bus 和 game event bus。
- `NeoForgeBiologyDictionaryClient`：NeoForge client-only 注册与初始化。

common 不主动判断当前平台；由平台入口决定何时调用 common 的哪一部分。

NeoForge 侧优先使用显式 listener 注册：

- 对 mod event bus 使用 `modEventBus.addListener(...)`。
- 对 game event bus 使用 `NeoForge.EVENT_BUS.addListener(...)`。
- 不要默认新增 `@SubscribeEvent` / `@EventBusSubscriber` 式注册。注解式订阅容易把注册入口、bus 类型和 dist 条件分散到多个类上，不利于对照 Fabric 注册和排查平台生命周期。
- 只有某个 NeoForge API 明确要求注解式订阅，或显式 listener 不能表达需求时，才使用 `@SubscribeEvent` / `@EventBusSubscriber`，并在代码附近说明原因。

## 网络迁移

当前网络方向是：

- common 定义 packet 类型、codec、方向、handler。
- `PacketPayloads.registerBuiltIn(Registrar)` 提供 payload 清单。
- Fabric 平台入口注册 payload type 和 receiver。
- NeoForge 平台入口在 `RegisterPayloadHandlersEvent` 中注册 payload。
- 发送 API 使用 `ClientNetApi` / `ServerNetApi` 窄服务。

packet 注册本身不强行抽成统一 `Platform.load` API，因为 Fabric 和 NeoForge 的 registrar、receiver context、注册阶段不同。

## 事件迁移

不做完整事件 facade，也不要求所有事件统一 API。

推荐模式：

1. common 中建立真实定义类，例如 `ClientEvents`、`ServerEvents`。
2. listener list 使用 `List.of(...)` 静态声明。
3. 字段标 `@PlatformEntry`。
4. Fabric/NeoForge 平台入口分别在正确 event bus / lifecycle 中逐项注册。
5. 旧 `platform/...Registry` 类暂时保留为空壳，写 `TODO: delete after port`，只用于迁移索引。

已采用该模式：

- `ClientEventRegistry` -> `ClientEvents`
- `ServerEventRegistry` -> `ServerEvents`
- `CommandRegistry` -> `Commands`

优先迁移实际业务需要的事件：

- server started / stopping
- player join / disconnect
- command registration
- server tick / client tick
- client world connect / disconnect
- resource/data reload
- overlay/world render

## Creative Tab 与 BiologyDictionaryItem

`BiologyDictionaryItem` 是一本带 custom data 的原版 writable book，不注册新 item。

creative tab entry 是平台注册内容，不是 item 本身。因此字段命名应表达“创造栏 entry”，例如：

```java
BIOLOGY_DICTIONARY_BOOK_CREATIVE_TAB_ENTRY
```

`CreativeModeTabs.TOOLS_AND_UTILITIES` 在 26.1.2 中仍是 private。当前使用 registry key：

```java
ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("tools_and_utilities"))
```

这是正常 registry key 构造方式，不需要为该字段单独加 accessor mixin。

## Mixin 与访问策略

- 两个平台实现相同且仅依赖 MC 原版内部访问时，优先放 common mixin。
- 如果平台 API 已覆盖需求，优先使用平台 API。
- Fabric 需要时维护 access widener。
- NeoForge 需要时维护 access transformer。
- common 业务不要依赖“某个平台把字段改 public”的隐式条件。

## ScreenRenderingContext 迁移策略

`ScreenRenderingContext` 是 GUI 迁移中的兼容层。1.21.11 代码只能作为语义参考，具体实现必须先核对 26.1.2 当前 API。

迁移规则：

- `ScreenRenderingContext` 的公开 API 必须与 1.21.11 保持一致，包括方法名、参数顺序、参数含义和 overload 集合。迁移时只能改实现，不能私自把调用契约改成更顺手的新形态；如果认为某个旧 API 不适合 26.1.2，必须先和维护者讨论后再改。
- 现有 `renderXxx` API 的语义和参数必须保留，迁移时优先改实现而不是改调用方契约。
- `renderXxx(..., float z, ...)` 中的 `z` 参数当前仅为兼容旧 API 保留，不要为了实现 `z` 引入额外渲染层、反射或 accessor；以后可单独评估删除。
- float 文本渲染只能作用于本模组显式调用 `ScreenRenderingContext.renderText(... float x, float y)` 的路径。
- 不要通过 mixin、access widener、access transformer 或全局替换改变原版、其他模组或普通 `GuiGraphicsExtractor.text(...)` 的渲染行为。
- 如果 26.1.2 API 可用临时 `pose` 变换实现本模组局部 float 坐标，应优先使用局部实现并在调用结束后恢复状态。
- 只有确实需要访问 26.1.2 私有状态时，才新增 accessor、access widener 或 access transformer；不要照搬 1.21.11 的访问项。

## ClientOnly 边界

- common 可以包含 `client` 包，但只能由平台 client 入口加载。
- 双端通用 init 链不得静态引用 `net.minecraft.client.*` 或本项目 `@ClientOnly` 类。
- 后续迁移 1.21.11 的 client-only 边界检查，理想形式是 Gradle verification task。

## 当前已知临时项

- `ClientEventRegistry`、`ServerEventRegistry`、`CommandRegistry` 是迁移占位，后续移植完成后删除。
- `PacketPayloads.registerBuiltIn(...)` 当前可为空，等待真实 packet 迁移。
- GUI/渲染相关代码需要持续对照 26.1.2 API，尤其是 `ScreenRenderingContext`。

## 验证命令

常用命令：

```bash
./gradlew check
./gradlew common:compileJava
./gradlew fabric:compileJava
./gradlew neoforge:compileJava
./gradlew checkCommonPlatformImports
./gradlew fabric:build
./gradlew neoforge:build
./gradlew fabric:runClient
./gradlew neoforge:runClient
```

迁移小步改动后至少跑：

```bash
./gradlew common:compileJava fabric:compileJava neoforge:compileJava checkCommonPlatformImports
```
