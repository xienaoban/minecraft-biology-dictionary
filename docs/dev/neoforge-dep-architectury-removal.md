# NeoForge 端 Architectury API 替换计划

## 总览

NeoForge 平台实现共 11 个文件，其中 `BiologyDictionaryNeoForge.java` 和 `BiologyDictionaryNeoForgeClient.java` 未使用 Architectury API。以下为其余 9 个文件中 Architectury API 的替换分析。

| 文件 | Architectury API | 替换难度 |
|---|---|---|
| `DevUtilsImpl` | `Platform.getEnvironment()` | 简单 |
| `ClientEventRegistryImpl` | `ClientLifecycleEvent` / `ClientPlayerEvent` / `ClientTickEvent` | 简单 |
| `ServerEventRegistryImpl` | `LifecycleEvent` / `PlayerEvent` | 简单 |
| `KeyMappingRegistryImpl` | `KeyMappingRegistry.register()` | 中等 |
| `ItemRegistryImpl` | `CreativeTabRegistry.appendStack()` | 中等 |
| `ClientNetApiImpl` | `NetworkManager.registerReceiver / sendToServer` | 复杂 |
| `ServerNetApiImpl` | `NetworkManager.registerReceiver / sendToPlayer` | 复杂 |
| `BiologyDictionaryNeoForgeServer` | `NetworkManager.registerS2CPayloadType` | 复杂 |

`CommandRegistryImpl` 已经是纯 NeoForge 实现，不需要改。

---

## 1. DevUtilsImpl — 环境检测 [简单]

### 当前代码

```java
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;

public static boolean isClient() {
    return Platform.getEnvironment() == Env.CLIENT;
}
```

### 替换方案

```java
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;

public static boolean isClient() {
    return FMLEnvironment.getDist() == Dist.CLIENT;
}
```

---

## 2. ClientEventRegistryImpl — 客户端事件 [简单]

### 当前代码

```java
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;

ClientLifecycleEvent.CLIENT_STARTED.register(listener::run);
ClientLifecycleEvent.CLIENT_STOPPING.register(listener::run);
ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> listener.run(Minecraft.getInstance()));
ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> listener.run(Minecraft.getInstance()));
ClientTickEvent.CLIENT_POST.register(listener::run);
```

### 替换方案

所有事件使用 `NeoForge.EVENT_BUS.addListener()` 注册，位于 **Game Event Bus**。

```java
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

public static void registerStarted(ClientEventRegistry.ClientListener listener) {
    NeoForge.EVENT_BUS.addListener(ClientStartedEvent.class, event -> listener.run(Minecraft.getInstance()));
}

public static void registerStopping(ClientEventRegistry.ClientListener listener) {
    NeoForge.EVENT_BUS.addListener(ClientStoppingEvent.class, event -> listener.run(Minecraft.getInstance()));
}

public static void registerWorldConnected(ClientEventRegistry.ClientListener listener) {
    NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, event -> listener.run(Minecraft.getInstance()));
}

public static void registerWorldDisconnecting(ClientEventRegistry.ClientListener listener) {
    NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> listener.run(Minecraft.getInstance()));
}

public static void registerEndTick(ClientEventRegistry.ClientListener listener) {
    NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> listener.run(Minecraft.getInstance()));
}
```

### Architectury 实现参考

Architectury 自身也是监听这些 NeoForge 事件：
- `ClientLifecycleEvent.CLIENT_SETUP` → 监听 `FMLClientSetupEvent`（注意这是 setup 阶段，不是 started）
- `CLIENT_PLAYER_JOIN` → `ClientPlayerNetworkEvent.LoggingIn`
- `CLIENT_PLAYER_QUIT` → `ClientPlayerNetworkEvent.LoggingOut`
- `CLIENT_POST` → `ClientTickEvent.Post`

---

## 3. ServerEventRegistryImpl — 服务端事件 [简单]

### 当前代码

```java
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;

LifecycleEvent.SERVER_STARTED.register(listener::run);
LifecycleEvent.SERVER_STOPPING.register(listener::run);
PlayerEvent.PLAYER_JOIN.register(player -> listener.run(player));
```

### 替换方案

```java
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.common.NeoForge;

public static void registerStarted(ServerEventRegistry.ServerListener listener) {
    NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, event -> listener.run(event.getServer()));
}

public static void registerStopping(ServerEventRegistry.ServerListener listener) {
    NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class, event -> listener.run(event.getServer()));
}

public static void registerPlayerLoggedIn(ServerEventRegistry.PlayerListener listener) {
    NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> listener.run((ServerPlayer) event.getEntity()));
}
```

### NeoForge Server 生命周期事件完整列表

`ServerLifecycleEvent` 的子类：
- `ServerAboutToStartEvent`
- `ServerStartingEvent`
- `ServerStartedEvent`
- `ServerStoppingEvent`
- `ServerStoppedEvent`

---

## 4. KeyMappingRegistryImpl — 按键注册 [中等]

### 当前代码

```java
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;

public static void registerKeyMapping(KeyMapping mapping) {
    KeyMappingRegistry.register(mapping);
}
```

### 问题

NeoForge 要求通过 `RegisterKeyMappingsEvent`（**Mod Bus** 事件）注册按键，不能在任意时机直接调用。

### 替换方案

采用与 `CommandRegistryImpl` 相同的收集模式：先收集 mapping，在事件触发时批量注册。

```java
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public final class KeyMappingRegistryImpl {
    private static final List<KeyMapping> mappings = new ArrayList<>();

    public static void registerKeyMapping(KeyMapping mapping) {
        mappings.add(mapping);
    }

    static {
        // 需要在 Client Mod Bus 上注册监听
        // 具体方式取决于 mod 初始化时如何获取 ModContainer
        // 参考 BiologyDictionaryNeoForgeClient 的 container 参数
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        mappings.forEach(event::register);
    }
}
```

### 注意事项

- `RegisterKeyMappingsEvent` 是 **Mod Bus** 事件（实现 `IModBusEvent`）
- 需要在 `BiologyDictionaryNeoForgeClient` 构造函数中通过 `container.registerExtensionPoint` 或类似方式注册监听
- 可以参考 `CommandRegistryImpl` 的 `NeoForge.EVENT_BUS.addListener()` 模式，但使用的是 Mod Bus

---

## 5. ItemRegistryImpl — 创造模式标签 [中等]

### 当前代码

```java
import dev.architectury.registry.CreativeTabRegistry;

public static void register(ResourceKey<CreativeModeTab> registryKey, ItemStack itemStack) {
    CreativeTabRegistry.appendStack(registryKey, itemStack);
}
```

### 问题

NeoForge 要求通过 `BuildCreativeModeTabContentsEvent`（**Mod Bus** 事件）注册，该事件对所有 tab 触发一次。

### 替换方案

同样采用收集模式：

```java
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;

public final class ItemRegistryImpl {
    private static final Map<ResourceKey<CreativeModeTab>, List<ItemStack>> pendingAdds = new HashMap<>();

    public static void register(ResourceKey<CreativeModeTab> registryKey, ItemStack itemStack) {
        pendingAdds.computeIfAbsent(registryKey, k -> new ArrayList<>()).add(itemStack);
    }

    static {
        // 需要在 Mod Bus 上注册监听
    }

    private static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
        List<ItemStack> stacks = pendingAdds.get(event.getTabKey());
        if (stacks != null) {
            stacks.forEach(stack -> event.accept(stack, TabVisibility.PARENT_AND_SEARCH_TABS));
        }
    }
}
```

### Architectury 实现参考

Architectury 的 `CreativeTabRegistryImpl` 也是同样的模式：在 `appendStack()` 时将物品存入 Map，在 `BuildCreativeModeTabContentsEvent` 触发时遍历 Map 调用 `event.accept()`。

---

## 6. 网络系统 [复杂]

### 当前架构

网络涉及 3 个文件：

- **`ServerNetApiImpl`** — 注册 C2S/S2C payload type + C2S handler + sendToPlayer
- **`ClientNetApiImpl`** — 注册 S2C handler + sendToServer
- **`BiologyDictionaryNeoForgeServer`** — 仅注册 S2C payload type（无 handler，专用服务端）

当前使用 Architectury 的 `NetworkManager`，核心 API：

```java
// 注册 S2C payload 类型（仅声明 type + codec，不含 handler）
NetworkManager.registerS2CPayloadType(type, codec);

// 注册接收器
NetworkManager.registerReceiver(NetworkManager.Side.S2C, type, codec, handler);
NetworkManager.registerReceiver(NetworkManager.Side.C2S, type, codec, handler);

// 发送
NetworkManager.sendToServer(payload);
NetworkManager.sendToPlayer(player, payload);
```

### NeoForge 原生网络架构

NeoForge 网络是**事件驱动**的，基于两条事件路径：

1. **`RegisterPayloadHandlersEvent`**（Mod Bus，common）— 注册 C2S handler 和 S2C type（可附带 handler）
2. **`RegisterClientPayloadHandlersEvent`**（Client Mod Bus）— 注册 S2C handler（客户端接收）

```java
// C2S 注册（Mod Bus）
modEventBus.addListener((RegisterPayloadHandlersEvent event) -> {
    PayloadRegistrar registrar = event.registrar("1");
    registrar.playToServer(type, codec, (payload, context) -> {
        ServerPlayer player = (ServerPlayer) context.player();
        // handle on main thread by default
    });
    registrar.playToClient(type, codec); // 仅注册 type + codec，无 handler
});

// S2C handler（Client Mod Bus）
clientModEventBus.addListener((RegisterClientPayloadHandlersEvent event) -> {
    event.register(type, (payload, context) -> {
        Minecraft client = Minecraft.getInstance();
        // handle
    });
});
```

发送 API：

```java
// C2S 发送
ClientPacketDistributor.sendToServer(payload);

// S2C 发送
PacketDistributor.sendToPlayer(serverPlayer, payload);
```

### 替换难点

1. **注册时机不同**：当前 `ServerNetApiImpl.register()` 和 `ClientNetApiImpl.register()` 是逐个 packet 调用的命令式注册。NeoForge 要求在事件回调中一次性注册所有 packet。需要改为收集模式（类似 CommandRegistryImpl）。

2. **Mod Bus 获取**：`RegisterPayloadHandlersEvent` 是 Mod Bus 事件，需要在 mod 构造函数中获取 `ModContainer` 并注册监听。当前 `BiologyDictionaryNeoForge` 构造函数没有保留 `ModContainer` 引用。

3. **`BiologyDictionaryNeoForgeServer`** 的存在增加了复杂度：专用服务端不加载客户端代码，所以 S2C handler 只能在 `RegisterClientPayloadHandlersEvent` 中注册（仅 client），而 S2C type 声明在 common 的 `RegisterPayloadHandlersEvent` 中。这与当前 Architectury 的 `registerS2CPayloadType()` 调用位置分离的逻辑不同。

4. **handler context 不同**：Architectury 的 context 回调提供 `NetworkManager.PacketContext`，NeoForge 的 `IPayloadContext` 接口不同。

### 建议的替换方案

```java
// ServerNetApiImpl.java
public final class ServerNetApiImpl {
    private static final List<PacketRegistration<?>> registrations = new ArrayList<>();

    public static <T extends Packet> void register(Class<T> clazz, Packet.Factory<T> factory) {
        PacketUtil.registerType(clazz);
        registrations.add(new PacketRegistration<>(clazz, factory));
    }

    static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        for (PacketRegistration<?> reg : registrations) {
            CustomPacketPayload.Type<?> type = PacketUtil.getType(reg.clazz);
            StreamCodec<FriendlyByteBuf, ?> codec = PacketUtil.generateCodec(reg.factory);

            if (PacketUtil.hasServerReceiver(reg.clazz)) {
                registrar.playToServer(type, codec, (payload, context) -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    ServerNetApi.Context ctx = new ServerNetApi.Context(PlayerUtils.getServer(player), player);
                    payload.serverReceive(ctx);
                });
            } else {
                // S2C type without handler (for server-side only)
                registrar.playToClient(type, codec);
            }
        }
    }

    public static void send(ServerPlayer player, Packet payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    private record PacketRegistration<T extends Packet>(Class<T> clazz, Packet.Factory<T> factory) {}
}
```

```java
// ClientNetApiImpl.java
public final class ClientNetApiImpl {
    static void onRegisterClientPayloads(RegisterClientPayloadHandlersEvent event) {
        // 遍历所有有 clientReceiver 的 packet，注册 S2C handler
    }

    public static void send(Packet payload) {
        ClientPacketDistributor.sendToServer(payload);
    }
}
```

### 注意事项

- 替换后 `BiologyDictionaryNeoForgeServer` 中的 `registerS2CPayloadType()` 调用可以移除，因为 type 声明已合并到 `ServerNetApiImpl.onRegisterPayloads()` 中
- `PayloadRegistrar` 需要传入协议版本字符串（如 `"1"`），需考虑后续版本更新时的兼容性
- `event.registrar(namespace)` 中的 namespace 参数控制 payload ID 的命名空间，需要确认是否影响现有 packet ID

---

## NeoForge 事件总线速查

| 事件 | 所属 Bus | 说明 |
|---|---|---|
| `RegisterPayloadHandlersEvent` | Mod Bus | 网络 payload 注册 |
| `RegisterClientPayloadHandlersEvent` | Client Mod Bus | 客户端 S2C handler 注册 |
| `RegisterKeyMappingsEvent` | Mod Bus | 按键映射注册 |
| `BuildCreativeModeTabContentsEvent` | Mod Bus | 创造模式标签构建 |
| `RegisterCommandsEvent` | Game Bus | 命令注册 |
| `ClientStartedEvent` / `ClientStoppingEvent` | Game Bus | 客户端生命周期 |
| `ClientPlayerNetworkEvent.LoggingIn/Out` | Game Bus | 客户端玩家加入/离开 |
| `ClientTickEvent.Post` | Game Bus | 客户端 tick |
| `ServerStartedEvent` / `ServerStoppingEvent` | Game Bus | 服务端生命周期 |
| `PlayerEvent.PlayerLoggedInEvent` | Game Bus | 玩家加入服务端 |

---

## 替换后可移除的 Architectury 依赖

如果全部替换完成，NeoForge 端将不再需要以下 Architectury 模块：

- `dev.architectury:architectury` (event, registry, platform)
- `dev.architectury:architectury-neoforge` (NeoForge 平台实现)

但需确认 `@ExpectPlatform` 注解处理器是否仍需要 Architectury Loom 插件（答案是：仍需要，`@ExpectPlatform` 是 Architectury Loom 的功能，与运行时 API 无关）。
