# 插件 API

Biology Dictionary 向其它模组提供两类扩展点：

- **注册型插件**——通过每类 registry 一个的插件接口注册自定义的**技能（skill）**、**实体属性（property）**、**实体显示顺序（entity order）**、**发现来源（discovery source）**，以及客户端的**组件（widget）**。
- **查询 API**——读取实体目录（实体列表、tag 归属）与发现状态（是否已发现、发现记录），客户端和服务端各有对应入口。

框架在初始化期间发现每个插件、调用对应的注册方法；此后注册表在整局游戏内不可变。注册只在启动时发生、运行时不再变更——下游系统（配置、网络、UI）读取的都是启动时固定的结果。

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

插件接口位于 `io.github.xienaoban.biologydictionary.api.plugin`。注意它们会引用模组内部包的类型（如 `core.skill` 的 `GeneralSkill`、`core.property` 的 `EntityProperty`、`gui.component` 的 `EntityPropertyWidget`）；第三方插件直接依赖整个模组 jar、按需使用这些类型即可。

## 声明你的插件

插件类必须实现所选接口、提供**公有无参构造函数**，并且只能标注一个对应的标记注解：

- 通用插件：`@BiologyDictionaryPlugin`
- 客户端插件：`@BiologyDictionaryClientPlugin`

同一个类不得同时标注两个注解。Fabric 还必须声明匹配的入口点；NeoForge 直接扫描注解。

### Fabric —— 注解与入口点（entrypoint）

除标注注解外，还要在 `fabric.mod.json` 中将该类声明在匹配的入口点下：通用插件用
`biologydictionary`，客户端插件用 `biologydictionary:client`：

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

## 查询 API

均在包 `io.github.xienaoban.biologydictionary.api` 下，是模组运行时状态的静态门面；状态缺失（world session 尚未就绪、类型未知或被黑名单、tag 不存在）一律以空结果返回——绝不返回 `null`、也不抛异常。

### `EntityInfoApi` —— 实体目录（不分端）

读取实体词典：哪些实体类型可追踪、它们的 tag 归属。

| 方法 | 返回 |
|---|---|
| `getEntityEntry(EntityType<?>)` | `Optional<EntityDictionaryEntry>`——单个类型的条目 |
| `getTotalEntities()` | `List<EntityDictionaryEntry>`——全部可追踪类型（已排序、已过滤黑名单） |
| `getTagEntities(groupId, tagId)` | `List<EntityDictionaryEntry>`——某 tag group 下某 tag 的条目 |
| `getBossEntities()` | boss 条目（默认 `boss` tag，背后是 `c:bosses` 约定 tag） |
| `getFriendlyEntities()` / `getNeutralEntities()` / `getEnemyEntities()` | 默认友好 / 中立 / 敌对 tag 的条目 |

tag group 与 tag 的 key 来自 `Lang`（如 `Lang.TAG_GROUP_DEFAULT`、`Lang.TAG_DEFAULT_BOSS`）；`getTagEntities` 是通用形式，其余方法是它的便捷封装。

### `ClientDiscoveryApi` —— 客户端发现状态

所有查询作用于当前本地玩家的缓存。缓存可能过期或不完整；需要权威结果时用 `ServerDiscoveryApi`。

| 方法 | 返回 |
|---|---|
| `isDiscovered(EntityType<?>)` | `boolean` |
| `getRecord(EntityType<?>)` | `Optional<DiscoveryRecord>` |
| `getDiscoveredEntities(entries)` | 把给定的条目列表（如 `EntityInfoApi.getTotalEntities()`）过滤为已发现的部分 |
| `recordDiscovery(source, entity)` | `boolean`——`true` 仅表示请求已提交，服务器仍可能拒绝 |

### `ServerDiscoveryApi` —— 服务端发现状态

权威数据。方法带 `ServerPlayer` 参数，因为发现状态按玩家区分。

| 方法 | 返回 |
|---|---|
| `isDiscovered(player, type)` | `boolean` |
| `getRecord(player, type)` | `Optional<DiscoveryRecord>` |
| `getDiscoveredEntities(player, entries)` | 把给定的条目列表过滤为已发现的部分 |
| `recordDiscovery(player, source, entity)` | `boolean`——`true` 表示本次事件实际产生了新发现 |

服务端查询均为纯查询、**不考虑创造模式**；如需 `creative || discovered` 语义，自行与 `player.isCreative()` 组合。

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

## 示例：注册一个实体属性

```java
import io.github.xienaoban.biologydictionary.api.BiologyDictionaryPlugin;
import io.github.xienaoban.biologydictionary.api.ExtraEntityPropertiesPlugin;

@BiologyDictionaryPlugin
public final class MyPlugin implements ExtraEntityPropertiesPlugin {
    @Override
    public void registerExtraEntityProperties(ExtraEntityPropertiesPlugin.Registrar registrar) {
        registrar.register(MyProperty.class, MyProperty.FACTORY);
    }
}
```

实体显示顺序同理：实现 `EntityOrdersPlugin`，在 `registerEntityOrders` 中注册 `EntityType`。

## 示例：注册一个发现来源

发现来源标注实体是*如何*被发现的（击杀、望远镜……），自带显示名、配置开关和双端校验。继承 `DiscoverySource`（位于 `core.discovery`）、按需 override，存到 `static` 字段以便后续触发，然后注册它。

```java
import io.github.xienaoban.biologydictionary.api.BiologyDictionaryPlugin;
import io.github.xienaoban.biologydictionary.api.DiscoverySourcesPlugin;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;

@BiologyDictionaryPlugin
public final class MyPlugin implements DiscoverySourcesPlugin {
    public static final DiscoverySource NET_CAPTURE = new DiscoverySource(
            Identifier.fromNamespaceAndPath("mymod", "net_capture")) {
        @Override public boolean clientCheck(DiscoverySource.ClientContext ctx) {
            return withinBlocks(ctx.player(), ctx.entity(), 5);     // 客户端闸门
        }
        @Override public boolean serverCheck(DiscoverySource.ServerContext ctx) {
            return withinBlocks(ctx.player(), ctx.entity(), 5);     // 服务端权威校验
        }
    };

    @Override
    public void registerDiscoverySources(DiscoverySourcesPlugin.Registrar registrar) {
        registrar.register(NET_CAPTURE);
    }
}
```

`displayName()` 默认从 id 派生翻译 key（`discovery_source.<namespace>.<path>`），一般无需 override；`isEnabled()`、`serverCheck(ServerContext)`、`clientCheck(ClientContext)` 默认放行。`clientCheck` 只在客户端被调用；服务端加载该类但不会触达任何客户端类型。

注册的来源**只在「生物辞典」发现策略下生效**；另外两种策略忽略插件来源。当你的触发条件满足时，触发它：

- 服务端：`ServerDiscoveryApi.recordDiscovery(player, source, entity)`
- 客户端：`ClientDiscoveryApi.recordDiscovery(source, entity)`

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

## 示例：查询

```java
// 全部可追踪条目，以及 boss 子集
List<EntityDictionaryEntry> all = EntityInfoApi.getTotalEntities();
List<EntityDictionaryEntry> bosses = EntityInfoApi.getBossEntities();

// 当前玩家已发现的 boss
List<EntityDictionaryEntry> discoveredBosses =
        ClientDiscoveryApi.getDiscoveredEntities(EntityInfoApi.getBossEntities());

// 服务端
List<EntityDictionaryEntry> discovered =
        ServerDiscoveryApi.getDiscoveredEntities(player, EntityInfoApi.getTotalEntities());
Optional<DiscoveryRecord> record = ServerDiscoveryApi.getRecord(player, EntityType.ZOMBIE);
```

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
