# TODO

本文件记录从 Architectury 版迁移到当前手动 multi-platform 架构时，除“common 源码/资源并入平台编译”之外的后续方案。

## 总体方向

- 不引入 Architectury、`@ExpectPlatform`、Architectury API 或 Architectury Gradle 插件。
- 不强行抹平 Fabric 与 NeoForge 的注册生命周期差异。
- `common` 主要负责定义业务对象、注册清单、packet、handler、通用初始化流程与纯 Minecraft API 逻辑。
- `fabric`、`neoforge` 平台入口分别按本平台风格消费 `common` 的定义并完成实际注册。
- 可以参考 `/mnt/e/project/ref/Field-Guide` 的窄服务接口思路，但不要照搬成一个过大的平台 facade。

## ClientOnly 边界检查

- 迁移 1.21.11 中 `common/src/testServer/java/.../ClientOnlyCheckTest.java` 的 ASM 检查逻辑。
- 保留或更新 `@ClientOnly`、`@ClientAndServer` 语义：
  - class-level `@ClientOnly` 表示该类只允许客户端加载。
  - method-level `@ClientOnly` 可用于隔离客户端调用入口。
  - `@ClientAndServer` 可用于桥接本项目 client-only 代码，但仍需避免直接引用 Minecraft client-only API。
- 短期可以继续放在 testServer/GameTest 中。
- 后续更理想的形式是 Gradle verification task，例如 `checkClientOnlyBoundaries`，让 `./gradlew check` 能直接发现越界引用。

## Common 定义，平台注册

- common 中避免直接调用 Fabric/NeoForge 注册 API。
- 对 item、data component、criterion、creative tab、command、network payload 等，优先采用“common 提供注册清单，平台入口消费”的模式。
- 示例形态：

```java
public final class ModPackets {
    public static void registerBuiltIn(Registrar registrar) {
        registrar.register(ExamplePacket.TYPE, ExamplePacket.CODEC, ExamplePacket::handleServer);
    }

    public interface Registrar {
        void register(...);
    }
}
```

- Fabric 入口使用 Fabric API 注册。
- NeoForge 入口使用 mod event bus、`DeferredRegister`、`PayloadRegistrar` 等 NeoForge 风格注册。

## 平台入口分层

- 建议固定入口职责：
  - `BiologyDictionary.init()`：双端通用初始化，不引用客户端类。
  - `BiologyDictionaryClient.init()`：客户端初始化，只由平台客户端入口调用。
  - `FabricBiologyDictionary`：Fabric server/common 注册与初始化。
  - `FabricBiologyDictionaryClient`：Fabric client 注册与初始化。
  - `NeoForgeBiologyDictionary`：NeoForge mod bus/game bus 注册与通用初始化。
  - `NeoForgeBiologyDictionaryClient`：NeoForge client-only 注册与初始化。
- common 不主动判断当前平台；由平台入口决定何时调用 common 的哪一部分。

## 网络迁移

- common 定义 packet 类型、codec、方向、业务 handler。
- 平台入口分别注册 payload type 和 receiver。
- common handler 避免直接依赖 Fabric/NeoForge context。
- 如需抽象 context，定义轻量 common context，例如：
  - server
  - player
  - enqueue/main-thread execution
  - side/direction
- 发送 API 可以做窄服务：
  - `sendToServer(packet)`
  - `sendToPlayer(player, packet)`
- packet 注册本身不必完全服务化，允许平台入口手写，以保持 Fabric/NeoForge 风格。

## 事件桥接

- 不需要一次性做完整事件 facade。
- 优先迁移业务真实需要的事件：
  - server started/stopping
  - player join/disconnect
  - command registration
  - server tick/client tick
  - client world connect/disconnect
  - resource/data reload
  - overlay/world render
- 可选方案：
  - 简单事件由平台入口直接调用 common manager。
  - 多处业务订阅同一事件时，再在 common 做自己的 listener list。

## 注册策略

- Fabric：
  - 直接使用 `Registry.register`、Fabric API callbacks、Fabric networking、Fabric client events。
  - 尽量保留 Fabric 官方模板风格。
- NeoForge：
  - 使用 `DeferredRegister`、mod event bus、game event bus、`PayloadRegistrar`。
  - 不为了和 Fabric 一致而绕开 NeoForge idiom。
- common 可以保存 `Supplier<T>` 或自定义 holder，但不要把 NeoForge `RegistryObject`/`DeferredHolder` 泄漏到 common API。

## ServiceLoader 使用边界

- 可以参考 Field-Guide 的 `Services`：
  - `IPlatformHelper`
  - `INetworkHelper`
  - `IRegistryHelper`
  - `IClientHelper`
- 本项目不建议一开始做大而全的 `Services.REGISTRY`。
- 更适合先做窄接口：
  - 平台信息与环境：`isModLoaded`、`isClientEnvironment`、`configDir`、`platformName`
  - 网络发送：`sendToServer`、`sendToPlayer`
  - 客户端工具中少量确实需要跨平台的动作
- 能由平台入口直接完成的注册，不必走 ServiceLoader。

## 客户端隔离

- common 可包含 `client` 包，但只能由平台客户端入口加载。
- 通用 init 链不得静态引用：
  - `net.minecraft.client.*`
  - client screen/renderer/key mapping 类
  - 本项目 `@ClientOnly` class
- key binding、screen、toast、overlay、entity renderer、配置界面入口等统一放到 client init 链。

## 配置与配置界面

- common 放配置模型、序列化、reload callback。
- 平台负责：
  - config 目录获取
  - optional dependency 检测
  - Fabric ModMenu 集成
  - NeoForge 配置界面集成
  - Cloth Config 依赖与入口声明

## Compat 与 optional dependency

- 避免主 init 链静态引用 optional mod 类。
- compat 建议分层：
  - common compat API/纯逻辑
  - platform detector
  - 第三方 mod implementation 延迟加载
- Fabric/NeoForge metadata 中分别声明 optional/recommended/required dependency。

## Mixin、Access Widener、Access Transformer

- 优先用 Mixin accessor/invoker 提供跨平台统一访问面。
- Fabric 需要时维护 access widener。
- NeoForge 需要时维护 access transformer。
- common 业务不要依赖“某个平台把字段改 public”这种隐式条件；通过 accessor/helper 调用。
- 每个平台 mixin json、metadata、资源路径分别维护。

## 迁移执行顺序

1. 迁移 `@ClientOnly`、`@ClientAndServer` 注解与边界检查。
2. 固定 common/server/client 初始化入口。
3. 迁移常量、logger、基础 util 与纯业务代码。
4. 建立 packet 定义与平台网络注册。
5. 迁移 item/data component/criterion/creative tab 等注册清单。
6. Fabric 入口按 Fabric 风格消费 common 定义并注册。
7. NeoForge 入口按 NeoForge 风格消费 common 定义并注册。
8. 迁移客户端 init：key binding、screen、toast、overlay、renderer。
9. 迁移配置、ModMenu/Cloth Config/NeoForge 配置入口。
10. 迁移 compat、mixin/access、resources、metadata。
11. 跑 `fabric:build`、`neoforge:build`、`fabric:runClient`、`neoforge:runClient`，逐项适配 API 差异。
