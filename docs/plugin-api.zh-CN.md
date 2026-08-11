# 插件 API

Biology Dictionary 允许其它模组通过**每类 registry 一个的插件接口**注册自定义的**技能（skill）**、**实体属性（property）**、**实体显示顺序（entity order）**、**发现来源（discovery source）**，以及客户端的**组件（widget）**。

框架在初始化期间发现每个插件、调用对应的注册方法；此后该注册表在整局游戏内不可变。注册只在启动时发生、运行时不再变更——下游系统（配置、网络、UI）读取的都是启动时固定的结果。

## 插件接口

每类 registry 有自己的插件接口、各一个注册方法。按需实现即可，一个类可以实现多个。

| 注册表 | 插件接口 | Registrar | 回调 | 运行侧 |
|---|---|---|---|---|
| 技能 | `BiologySkillsPlugin` | `BiologySkillsPlugin.Registrar` | `registerBiologySkills` | 通用 |
| 额外实体属性 | `ExtraEntityPropertiesPlugin` | `ExtraEntityPropertiesPlugin.Registrar` | `registerExtraEntityProperties` | 通用 |
| 实体显示顺序 | `EntityOrdersPlugin` | `EntityOrdersPlugin.Registrar` | `registerEntityOrders` | 通用 |
| 发现来源 | `DiscoverySourcesPlugin` | `DiscoverySourcesPlugin.Registrar` | `registerDiscoverySources` | 通用 |
| 组件 | `EntityPropertyWidgetsPlugin` | `EntityPropertyWidgetsPlugin.Registrar` | `registerEntityPropertyWidgets` | 仅客户端 |

通用接口在客户端和专用服务端都运行。组件接口仅限客户端（`@ClientOnly`），因为组件只存在于客户端。

## 声明你的插件

插件类必须实现所选接口、提供**公有无参构造函数**，并且只能标注一个对应的标记注解：

- 通用插件：`@BiologyDictionaryPlugin`
- 客户端插件：`@BiologyDictionaryClientPlugin`

同一个类不得同时标注两个注解。Fabric 还必须声明匹配的入口点；NeoForge 直接扫描注解。

### Fabric —— 注解与入口点（entrypoint）

除标注注解外，还要在 `fabric.mod.json` 中将该类声明在匹配的入口点下：通用插件用
`biologydictionary`，客户端（组件）插件用 `biologydictionary:client`：

```json
{
  "entrypoints": {
    "biologydictionary": ["com.example.MyPlugin"]
  }
}
```

### NeoForge —— 注解扫描

在类上标注注解，加载器会在启动时扫描模组字节码寻找它。通用插件用 `@BiologyDictionaryPlugin`，客户端插件用 `@BiologyDictionaryClientPlugin`：

```java
@BiologyDictionaryPlugin
public final class MyPlugin implements BiologySkillsPlugin { ... }
```

Fabric 上缺失或类型不匹配的标记注解都会终止加载。插件内部的注册代码在两个加载器上完全一致；若该类实现了多个插件接口，每个 registry 只派发自己关心的那个回调。

## API 类型

均在包 `io.github.xienaoban.biologydictionary.api` 下：

- 插件接口：`BiologySkillsPlugin` / `ExtraEntityPropertiesPlugin` / `EntityOrdersPlugin` / `DiscoverySourcesPlugin` / `EntityPropertyWidgetsPlugin`（各 Registrar 为接口的嵌套类型）
- 发现 API：`ServerDiscoveryApi`（服务端）/ `ClientDiscoveryApi`（客户端）/ `DiscoveryProgress` / `DiscoverySource` / `DiscoveryRecord`
- `@BiologyDictionaryPlugin`（通用）、`@BiologyDictionaryClientPlugin`（客户端）

## 示例：注册一个技能

按内建技能的方式编写你的技能（一个实现 `GeneralSkill` 并带静态 `Meta` 的类），然后注册它：

```java
import io.github.xienaoban.biologydictionary.api.BiologyDictionaryPlugin;
import io.github.xienaoban.biologydictionary.api.BiologySkillsPlugin;

@BiologyDictionaryPlugin
public final class MyPlugin implements BiologySkillsPlugin {
    @Override
    public void registerBiologySkills(BiologySkillsPlugin.Registrar registrar) {
        registrar.register(MySkill.class, MySkill.META);
    }
}
```

（Fabric 上，把 `com.example.MyPlugin` 声明在 `biologydictionary` 入口点下，替代注解。）

属性和实体顺序同理，只是各自的插件接口和 Registrar 不同。

## 示例：注册一个组件（仅客户端）

```java
import io.github.xienaoban.biologydictionary.api.BiologyDictionaryClientPlugin;
import io.github.xienaoban.biologydictionary.api.EntityPropertyWidgetsPlugin;

@BiologyDictionaryClientPlugin
public final class MyClientPlugin implements EntityPropertyWidgetsPlugin {
    @Override
    public void registerEntityPropertyWidgets(EntityPropertyWidgetsPlugin.Registrar registrar) {
        registrar.register(MyWidget.class, MyWidget.FACTORY);
    }
}
```

（Fabric 上，把它声明在 `biologydictionary:client` 入口点下。）

## 示例：注册一个发现来源

发现来源标注实体是*如何*被发现的（击杀、望远镜……），自带显示名、配置开关和双端校验。继承 `DiscoverySource`、按需 override，存到 `static` 字段以便后续触发，然后注册它。

```java
import io.github.xienaoban.biologydictionary.api.BiologyDictionaryPlugin;
import io.github.xienaoban.biologydictionary.api.DiscoverySource;
import io.github.xienaoban.biologydictionary.api.DiscoverySourcesPlugin;

@BiologyDictionaryPlugin
public final class MyPlugin implements DiscoverySourcesPlugin {
    public static final DiscoverySource NET_CAPTURE = new DiscoverySource(
            Identifier.fromNamespaceAndPath("mymod", "net_capture")) {
        @Override public boolean clientCheck(ClientContext ctx) {
            return withinBlocks(ctx.player(), ctx.entity(), 5);     // 客户端闸门
        }
        @Override public boolean serverCheck(ServerContext ctx) {
            return withinBlocks(ctx.player(), ctx.entity(), 5);     // 服务端权威校验
        }
    };

    @Override
    public void registerDiscoverySources(DiscoverySourcesPlugin.Registrar registrar) {
        registrar.register(NET_CAPTURE);
    }
}
```

`displayName()` 默认从 id 派生翻译 key（`discovery_source.<namespace>.<path>`），一般无需 override；`isEnabled()`、`serverCheck(ServerContext)`、`clientCheck(ClientContext)` 默认放行。`clientCheck` 及其 `ClientContext` 仅限客户端。本模组中出现的 `@ClientOnly` 注解是内部使用的，用于替代 Fabric 的 `@Environment`；第三方模组无需关心。

注册的来源**只在「生物辞典」发现策略下生效**；另外两种策略忽略插件来源。当你的触发条件满足时，触发它：

- 服务端：`ServerDiscoveryApi.recordDiscovery(source, player, entity)`
- 客户端：`ClientDiscoveryApi.recordDiscovery(source, entity)`

## 契约与生命周期

- 框架用无参构造函数实例化你的插件，并在启动初始化期间精确调用每个注册回调一次。
- 只在回调内注册。不要持有 Registrar，也不要稍后再注册——注册只在启动时进行一次，之后不再进入。
- 内建项先注册，随后才是各插件。插件*之间*的顺序不保证（取决于加载器的发现顺序），所以不要依赖另一个插件的项已经存在。
- 每个注册表都拒绝重复（例如技能短名冲突）并以抛异常方式报错——请用唯一的名字注册。
- 抛异常的插件会被隔离：错误被记录，其它插件和内建项的注册继续进行，不会导致游戏崩溃。

## 发现机制

发现交给各加载器的官方机制，而非 classpath 扫描：

- **Fabric**：加载器解析你声明的入口点。
- **NeoForge**：加载器的注解扫描（`ModList` scan data）找到 `@BiologyDictionaryPlugin` / `@BiologyDictionaryClientPlugin`。
