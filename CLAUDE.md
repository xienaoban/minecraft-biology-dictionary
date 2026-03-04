# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Biology Dictionary is a Minecraft utility mod for viewing and modifying mob properties. It supports both Fabric and NeoForge using the Architectury framework, allowing code sharing between platforms. The mod does not add new blocks or entities - it only adds a book item implemented via writable book + NBT/components.

## Build Commands

### Building the Project
```bash
./gradlew build                    # Build all platforms
./gradlew fabric:build             # Build Fabric only
./gradlew neoforge:build           # Build NeoForge only
```

### Development
```bash
./gradlew fabric:runClient         # Run Fabric client
./gradlew fabric:runServer         # Run Fabric server
./gradlew neoforge:runClient       # Run NeoForge client
./gradlew neoforge:runServer       # Run NeoForge server
```

### Testing
```bash
./gradlew fabric:testServer        # Run Fabric server tests
./gradlew fabric:testClient        # Run Fabric client tests
./gradlew neoforge:testServer      # Run NeoForge server tests
./gradlew neoforge:testClient      # Run NeoForge client tests
```

### Other Useful Commands
```bash
./gradlew fabric:compileJava       # Compile Fabric Java sources
./gradlew neoforge:compileJava     # Compile NeoForge Java sources
./gradlew dependencies             # View dependency tree
```

## Architecture

### Module Structure

The project uses Architectury's three-module pattern:

- **`common/`** - Shared code between Fabric and NeoForge
  - Contains all core game logic, UI, property system, widgets
  - Uses `@ExpectPlatform` annotations for platform-specific abstractions
  - Mixins defined in `common/src/main/resources/biologydictionary.mixins.json`

- **`fabric/`** - Fabric-specific implementations
  - Implements platform abstractions (e.g., `ClientNetApiImpl`, `ServerEventRegistryImpl`)
  - Fabric mod metadata in `fabric/src/main/resources/fabric.mod.json`

- **`neoforge/`** - NeoForge-specific implementations
  - Parallel implementations to Fabric, using NeoForge APIs
  - NeoForge metadata in `neoforge/src/main/resources/META-INF/neoforge.mods.toml`

### Core Systems

#### Property System (`common/src/main/java/io/github/xienaoban/biologydictionary/core/property/`)
The heart of the mod - handles reading/writing entity properties via NBT:
- `EntityProperty<E>` - Base interface for entity properties
- Built-in types: `BooleanProperty`, `IntProperty`, `StringProperty`, etc.
- Vanilla types: `BlockPosProperty`, `ItemStackListProperty`, `VariantProperty`, etc.
- `VanillaEntityProperties` - Auto-generated properties for vanilla entities
- `EntityVariantPropertyBundle` - Groups variant properties (horse markings, panda genes)

#### Widget System (`common/src/main/java/io/github/xienaoban/biologydictionary/core/widget/`)
UI components for displaying entity properties:
- `EntityPropertyWidget<E>` - Base widget interface
- **Branch widgets** - Complex widgets that may contain sub-widgets
- **Leaf widgets** - Simple atomic widgets
- **Variant widgets** - Entity variant selection
- Widgets use factory pattern and auto-register via `registerBuiltIn()`

#### Platform Abstractions
Common code calls abstract methods with `@ExpectPlatform` annotations:
- `ClientNetApi` / `ServerNetApi` - Networking (packet registration, sending)
- `ClientEventRegistry` / `ServerEventRegistry` - Event handling
- `KeyMappingRegistry` - Key bindings
- `ItemRegistry` - Item registration

Platform-specific implementations in `fabric/` and `neoforge/` provide the actual behavior.

#### Skill System (`common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/`)
Game manipulation abilities:
- `GeneralSkill` - General actions (highlight entities, spawn eggs)
- `EntityTargetedSkill` - Actions targeting specific entities (set age, set variant, mute)
- Skills use a cost system with server-side validation

#### Networking (`common/src/main/java/io/github/xienaoban/biologydictionary/net/`)
- `Packet` interface for all network packets
- Payload-based packets in `net/payload/`
- Bidirectional client-server communication

#### Configuration (`common/src/main/java/io/github/xienaoban/biologydictionary/config/`)
- YAML-based configuration using SnakeYAML
- `@ConfigCategory`, `@ConfigEntry` annotations
- Cloth Config integration for UI
- Hot-reloadable with server sync

### Mixins

Mixins are defined in `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/`:
- `entity/` - Entity-related mixins (AgeableMob, Animal, Horse, etc.)
- `loot/` - Loot table manipulation
- `rendering/` - Client-side rendering (client mixins only)

Mixin config: `common/src/main/resources/biologydictionary.mixins.json`

### Testing

Uses Minecraft GameTest framework:
- Test sources in `common/src/testServer/java/` and `fabric/src/testServer/java/`
- Key tests:
  - `VanillaEntityNbtTest` - NBT tag collection and property generation
  - `VanillaEntityCollectionTest` - Entity collection behavior
  - `VanillaEntityBehaviorTest` - Entity behavior testing
  - `RegistrarsTest` - Widget/property registration
- Test utilities include JavaParser (AST parsing), Procyon/Vineflower (decompilation)

## Key Patterns

### Platform Abstraction Pattern
```java
// In common code
public abstract class ClientNetApi {
    @ExpectPlatform
    public static <T extends Packet> void register(Class<T> clazz, Packet.Factory<T> factory);
}

// Platform implementations provide actual behavior
```

### Property Pattern
```java
// Properties read/write from NBT and can sync with entities
void readFrom(CompoundTag nbt);
void writeTo(CompoundTag nbt);
void getFrom(E entity);    // Read entity state to property
void setTo(E entity);      // Write property state to entity
```

### Widget Factory Pattern
```java
public static final EntityPropertyWidget.Factory<LaborEntity, LaborEntityHealthWidget> FACTORY =
    new EntityPropertyWidget.Factory<>(LaborEntityHealthWidget::new);
```

## Version Information

- Minecraft: 1.21.11
- Java: 21
- Fabric Loader: 0.18.4
- Fabric API: 0.141.3+1.21.11
- NeoForge: 21.11.38-beta
- Architectury API: 19.0.1
- Cloth Config: 21.11.153

## Adding New Features

1. **New Entity Property**: Add to `core/property/`, implement `EntityProperty<E>`
2. **New Widget**: Add to `core/widget/`, implement `EntityPropertyWidget<E>`, register in `registerBuiltIn()`
3. **New Skill**: Add to `core/skill/entity/`, extend appropriate skill class
4. **Platform-Specific Code**: Use `@ExpectPlatform` in common, implement in both `fabric/` and `neoforge/`

## Important Notes

- Always implement features for both Fabric and NeoForge platforms
- Mixins should be in common and work for both platforms
- The access widener is in `common/src/main/resources/biologydictionary.accesswidener`
- Main entry points: `BiologyDictionaryFabric` / `BiologyDictionaryNeoForge` (server), `BiologyDictionaryFabricClient` / `BiologyDictionaryNeoForgeClient` (client)

## Code Style & Coding Rules

- Avoid using the `this.` qualifier in code unless explicitly necessary. Follow the code style of existing code in the project.
- Unless absolutely necessary, avoid using the fully qualified name of the class; instead, use `import`.
- Do not modify any existing code without a valid justification.
- All temporary files generated during development (i.e., files that will not be committed to Git in the end) must be stored in the dedicated directory `<project-root>/tmp/claude/mc/`. Do not scatter temporary files arbitrarily across the project. Especially pay attention to the output directory when executing `unzip`, `tar -xf`, `jar -xf` and so on (maybe you should `cd` first).
- Prioritize consulting and referencing Minecraft's official first-party code over searching for solutions online.
- If online resources for Minecraft code are unavoidable, always verify version compatibility between the retrieved materials and your target Minecraft version. Implementations of Minecraft vary significantly across different versions, which may lead to compatibility issues.
- Avoid using reflection in source code (except test code), as obfuscation is applied to field and method names in the production Minecraft runtime. Prefer using Mixin instead.
