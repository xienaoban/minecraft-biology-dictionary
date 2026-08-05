# Plugin API

Biology Dictionary lets other mods register custom **skills**, **entity properties**, **entity display order**, and client-side **widgets** through per-registry plugin interfaces.

The framework discovers each plugin once during initialization and calls its registration method. That registry is then immutable for the rest of the game — registration happens only at startup, so downstream systems (config, networking, the UI) read a fixed result.

## Plugin interfaces

Each registry has its own plugin interface with one registration method. Implement whichever you need — a single class may implement several.

| Registry | Plugin interface | Registrar | Callback | Side |
|---|---|---|---|---|
| Skills | `BiologySkillsPlugin` | `BiologySkillsRegistrar` | `registerSkills` | common |
| Extra entity properties | `ExtraEntityPropertiesPlugin` | `ExtraEntityPropertiesRegistrar` | `registerProperties` | common |
| Entity display order | `EntityOrderPlugin` | `EntityOrderRegistrar` | `registerEntityOrder` | common |
| Widgets | `EntityPropertyWidgetsPlugin` | `EntityPropertyWidgetsRegistrar` | `registerWidgets` | client only |

The common interfaces run on both the client and the dedicated server. The widget interfaces are client-only (`@ClientOnly`), because widgets only exist on the client.

## Declaring your plugin

The plugin class must implement the chosen interface(s), expose a **public no-arg constructor**, and
carry exactly one matching marker annotation:

- common plugin: `@BiologyDictionaryPlugin`
- client plugin: `@BiologyDictionaryClientPlugin`

A class must not carry both annotations. Fabric additionally requires a matching entrypoint;
NeoForge discovers the annotated class directly.

### Fabric — annotation and entrypoint

In addition to the annotation, declare the class under the matching entrypoint in
`fabric.mod.json`: `biologydictionary` for common plugins and `biologydictionary:client` for
client (widget) plugins:

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

On Fabric, a missing or mismatched marker annotation terminates loading. The registration code is
identical on both loaders; if a plugin implements several plugin interfaces, each registry
dispatches the callback it cares about.

## API types

All in package `io.github.xienaoban.biologydictionary.api`:

- `BiologySkillsPlugin` / `BiologySkillsRegistrar`
- `ExtraEntityPropertiesPlugin` / `ExtraEntityPropertiesRegistrar`
- `EntityOrderPlugin` / `EntityOrderRegistrar`
- `EntityPropertyWidgetsPlugin` / `EntityPropertyWidgetsRegistrar`
- `@BiologyDictionaryPlugin` (common), `@BiologyDictionaryClientPlugin` (client)

## Example: registering a skill

Author your skill the way built-in skills do (a class implementing `GeneralSkill` with a static `Meta`), then register it:

```java
import io.github.xienaoban.biologydictionary.api.BiologyDictionaryPlugin;
import io.github.xienaoban.biologydictionary.api.BiologySkillsPlugin;
import io.github.xienaoban.biologydictionary.api.BiologySkillsRegistrar;

@BiologyDictionaryPlugin
public final class MyPlugin implements BiologySkillsPlugin {
    @Override
    public void registerSkills(BiologySkillsRegistrar registrar) {
        registrar.register(MySkill.class, MySkill.META);
    }
}
```

(On Fabric, declare `com.example.MyPlugin` under the `biologydictionary` entrypoint instead of the annotation.)

Properties and entity order follow the same shape with their own plugin interface and registrar.

## Example: registering a widget (client only)

```java
import io.github.xienaoban.biologydictionary.api.BiologyDictionaryClientPlugin;
import io.github.xienaoban.biologydictionary.api.EntityPropertyWidgetsPlugin;
import io.github.xienaoban.biologydictionary.api.EntityPropertyWidgetsRegistrar;

@BiologyDictionaryClientPlugin
public final class MyClientPlugin implements EntityPropertyWidgetsPlugin {
    @Override
    public void registerWidgets(EntityPropertyWidgetsRegistrar registrar) {
        registrar.register(MyWidget.class, MyWidget.FACTORY);
    }
}
```

(On Fabric, declare it under the `biologydictionary:client` entrypoint.)

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
