# Plugin API

Biology Dictionary exposes two kinds of extension points for other mods:

- **Registration plugins** — register custom **skills**, **entity properties**, **entity display order**, **discovery sources**, and client-side **widgets** through per-registry plugin interfaces.
- **Query APIs** — read the entity catalog (entity list, tag membership) and the discovery state (discovered / record), from both client and server.

The framework discovers each plugin once during initialization and calls its registration method; registries are immutable afterwards. Registration happens only at startup — downstream systems (config, networking, UI) read a fixed result.

## Plugin interfaces

Each registry has its own plugin interface with one registration method. Implement whichever you need — a single class may implement several.

| Registry | Plugin interface | Registrar | Callback | Side |
|---|---|---|---|---|
| Skills | `BiologySkillsPlugin` | `BiologySkillsPlugin.Registrar` | `registerBiologySkills` | common |
| Extra entity properties | `ExtraEntityPropertiesPlugin` | `ExtraEntityPropertiesPlugin.Registrar` | `registerExtraEntityProperties` | common |
| Entity display order | `EntityOrdersPlugin` | `EntityOrdersPlugin.Registrar` | `registerEntityOrders` | common |
| Discovery sources | `DiscoverySourcesPlugin` | `DiscoverySourcesPlugin.Registrar` | `registerDiscoverySources` | common |
| Widgets | `EntityPropertyWidgetsPlugin` | `EntityPropertyWidgetsPlugin.Registrar` | `registerEntityPropertyWidgets` | client only |

The common interfaces run on both the client and the dedicated server. The widget interface is client-only (`@ClientOnly`), because widgets only exist on the client.

Plugin interfaces live in `io.github.xienaoban.biologydictionary.api.plugin`. Note that they reference types from the mod's internal packages (e.g. `GeneralSkill` in `core.skill`, `EntityProperty` in `core.property`, `EntityPropertyWidget` in `gui.component`); third-party plugins simply depend on the whole mod jar and use those types directly.

## Declaring your plugin

The plugin class must implement the chosen interface(s), expose a **public no-arg constructor**, and carry exactly one matching marker annotation:

- common plugin: `@BiologyDictionaryPlugin`
- client plugin: `@BiologyDictionaryClientPlugin`

A class must not carry both annotations. Fabric additionally requires a matching entrypoint; NeoForge discovers the annotated class directly.

### Fabric — annotation and entrypoint

In addition to the annotation, declare the class under the matching entrypoint in `fabric.mod.json`: `biologydictionary` for common plugins and `biologydictionary:client` for client plugins:

```json
{
  "entrypoints": {
    "biologydictionary": ["com.example.MyPlugin"]
  }
}
```

### NeoForge — annotation scan

Annotate the class; the loader scans mod bytecode for it at startup. Use `@BiologyDictionaryPlugin` for common plugins, `@BiologyDictionaryClientPlugin` for client plugins:

```java
@BiologyDictionaryPlugin
public final class MyPlugin implements BiologySkillsPlugin { ... }
```

On Fabric, a missing or mismatched marker annotation terminates loading. The registration code is identical on both loaders; if a plugin implements several plugin interfaces, each registry dispatches the callback it cares about.

## Query APIs

All in package `io.github.xienaoban.biologydictionary.api`. They are static facades over the mod's runtime state; absent state (no world session yet, unknown/blacklisted entity, missing tag) is reported as an empty result — never `null` or an exception.

### `EntityInfoApi` — entity catalog (no client/server distinction)

Reads the entity dictionary: which entity types are trackable, and their tag membership.

| Method | Returns |
|---|---|
| `getEntityEntry(EntityType<?>)` | `Optional<EntityDictionaryEntry>` — entry of a single type |
| `getTotalEntities()` | `List<EntityDictionaryEntry>` — all trackable types (sorted, blacklist-filtered) |
| `getTagEntities(groupId, tagId)` | `List<EntityDictionaryEntry>` — entries of a tag in a tag group |
| `getBossEntities()` | boss entries (default `boss` tag, backed by the `c:bosses` convention tag) |
| `getFriendlyEntities()` / `getNeutralEntities()` / `getEnemyEntities()` | entries of the default friendly / neutral / enemy tags |

Tag group and tag keys come from `Lang` (e.g. `Lang.TAG_GROUP_DEFAULT`, `Lang.TAG_DEFAULT_BOSS`); `getTagEntities` is the general form, the other methods are conveniences over it.

### `ClientDiscoveryApi` — client-side discovery state

All queries act on the current local player's cache. The cache may be stale or incomplete; for authoritative answers use `ServerDiscoveryApi`.

| Method | Returns |
|---|---|
| `isDiscovered(EntityType<?>)` | `boolean` |
| `getRecord(EntityType<?>)` | `Optional<DiscoveryRecord>` |
| `getDiscoveredEntities(entries)` | filters a given entry list (e.g. `EntityInfoApi.getTotalEntities()`) down to the discovered ones |
| `recordDiscovery(source, entity)` | `boolean` — `true` means the request was submitted; the server may still reject it |

### `ServerDiscoveryApi` — server-side discovery state

Authoritative. Methods take a `ServerPlayer` because discovery state is per player.

| Method | Returns |
|---|---|
| `isDiscovered(player, type)` | `boolean` |
| `getRecord(player, type)` | `Optional<DiscoveryRecord>` |
| `getDiscoveredEntities(player, entries)` | filters a given entry list down to the discovered ones |
| `recordDiscovery(player, source, entity)` | `boolean` — `true` means this event actually resulted in a new discovery |

All server queries are pure and do **not** consider creative mode; combine with `player.isCreative()` yourself if you want `creative || discovered` semantics.

## Example: registering a skill

Author your skill the way built-in skills do (a class implementing `GeneralSkill` with a static `Meta`), then register it:

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

(On Fabric, declare `com.example.MyPlugin` under the `biologydictionary` entrypoint instead of the annotation.)

## Example: registering an entity property

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

Entity display order follows the same shape: implement `EntityOrdersPlugin` and register `EntityType`s in `registerEntityOrders`.

## Example: registering a discovery source

A discovery source labels *how* an entity was discovered (kill, telescope, …) and carries its own display name, config gate, and per-side validation. Subclass `DiscoverySource` (in `core.discovery`), override what you need, store it in a `static` field so you can fire it later, then register it.

```java
import io.github.xienaoban.biologydictionary.api.BiologyDictionaryPlugin;
import io.github.xienaoban.biologydictionary.api.DiscoverySourcesPlugin;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;

@BiologyDictionaryPlugin
public final class MyPlugin implements DiscoverySourcesPlugin {
    public static final DiscoverySource NET_CAPTURE = new DiscoverySource(
            Identifier.fromNamespaceAndPath("mymod", "net_capture")) {
        @Override public boolean clientCheck(DiscoverySource.ClientContext ctx) {
            return withinBlocks(ctx.player(), ctx.entity(), 5);     // client-side gate
        }
        @Override public boolean serverCheck(DiscoverySource.ServerContext ctx) {
            return withinBlocks(ctx.player(), ctx.entity(), 5);     // server-authoritative
        }
    };

    @Override
    public void registerDiscoverySources(DiscoverySourcesPlugin.Registrar registrar) {
        registrar.register(NET_CAPTURE);
    }
}
```

`displayName()` derives from the id as `discovery_source.<namespace>.<path>` by default, so it usually needs no override; `isEnabled()`, `serverCheck(ServerContext)`, and `clientCheck(ClientContext)` default to permissive. `clientCheck` is only ever invoked on the client; servers load the class but never touch client types.

A registered source is effective **only under the Biology Dictionary discovery strategy**; the other two strategies ignore plugin sources. When your trigger condition is met, fire it:

- Server: `ServerDiscoveryApi.recordDiscovery(player, source, entity)`
- Client: `ClientDiscoveryApi.recordDiscovery(source, entity)`

## Example: registering a widget (client only)

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

(On Fabric, declare it under the `biologydictionary:client` entrypoint.)

## Example: querying

```java
// all trackable entries, and the boss subset
List<EntityDictionaryEntry> all = EntityInfoApi.getTotalEntities();
List<EntityDictionaryEntry> bosses = EntityInfoApi.getBossEntities();

// boss entries the current player has discovered
List<EntityDictionaryEntry> discoveredBosses =
        ClientDiscoveryApi.getDiscoveredEntities(EntityInfoApi.getBossEntities());

// server side
List<EntityDictionaryEntry> discovered =
        ServerDiscoveryApi.getDiscoveredEntities(player, EntityInfoApi.getTotalEntities());
Optional<DiscoveryRecord> record = ServerDiscoveryApi.getRecord(player, EntityTypes.ZOMBIE);
```

## Contract & lifecycle

- The framework instantiates your plugin with its no-arg constructor and calls each registration callback exactly once, during startup initialization.
- Register only inside the callback. Do not retain the registrar or register later — registration runs once during startup and is not re-entered.
- Built-in entries register first, then plugins. Ordering *across* plugins is not guaranteed (it follows loader discovery order), so do not depend on another plugin's entries being present.
- Each registry rejects duplicates (e.g. a skill short-name clash) by throwing — register under unique names.
- A throwing plugin is isolated: the error is logged and registration continues for other plugins and built-ins. It does not crash the game.

## Discovery

Discovery is delegated to each loader's official mechanism rather than classpath scanning:

- **Fabric**: the loader resolves your declared entrypoint.
- **NeoForge**: the loader's annotation scan (`ModList` scan data) finds `@BiologyDictionaryPlugin` / `@BiologyDictionaryClientPlugin`.
