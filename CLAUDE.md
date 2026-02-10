# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Biology Dictionary is a Minecraft Fabric mod (1.21.11) that allows players to view and modify entity/mob properties in-game through an item-based interface. Players can inspect any living entity's attributes and manipulate them via a GUI.

## Build Commands

```bash
# Build the project
./gradlew build

# Run client for testing
./gradlew runClient

# Run server for testing
./gradlew runServer

# Run test server (Fabric GameTest framework)
./gradlew testServer

# Run test client
./gradlew testClient

# Generate IDE run configurations (IntelliJ)
./gradlew genIntellijRuns

# Clean build artifacts
./gradlew clean build
```

## Architecture

### Entry Points
- `BiologyDictionary.java` - Server-side entry point, initializes all core systems
- `BiologyDictionaryClient.java` - Client-side entry point

### Core Systems

**EntityManager** (`core/EntityManager.java`)
- Singleton that manages all entity type metadata
- Initializes on server start after joining a world (requires a Level instance)
- Creates entity classification trees and tag groups for filtering
- Maps entity classes to EntityTypes and maintains sort order
- Call `BD.forceInitialize()` to trigger class initialization if needed

**EntityProperties** (`core/property/EntityProperties.java`)
- Central registry for entity property definitions
- Two types of properties:
  - **Vanilla Properties**: Wrappers for vanilla Minecraft entity data (health, movement, AI, etc.)
  - **Extra Properties**: Custom mod-specific properties
- Properties are instantiated per-entity via class hierarchy walking

**Widget System** (`core/widget/`)
- Hierarchical GUI components for displaying entity properties
- **Branch widgets**: Composite nodes that group related properties (e.g., health subtree)
- **Leaf widgets**: Terminal nodes for specific features
- **Variant widgets**: Handle entity variants (horse colors, villager professions, etc.)

**Skill System** (`core/skill/`)
- Actions that can be performed on entities
- General skills (highlighting, spawn eggs)
- Entity-targeted skills (age modification, variant changes)

**Networking** (`common/net/`)
- Custom packet system for client-server communication
- `ServerNetManager` and `ClientNetManager` handle respective endpoints
- Used for entity property synchronization

### Initialization Order

The mod uses lazy initialization through static method calls:
1. `BiologyDictionary` constructor fires on mod load
2. `ServerEventRegistry` defers actual init until server starts
3. `EntityManager.init()` requires a Level (only available after world join)
4. Systems call `forceInitialize()` to trigger class init blocks

### Event Registry Pattern

Instead of direct event registration, the code uses custom registry classes:
- `ServerEventRegistry` - Server lifecycle events
- `ClientEventRegistry` - Client lifecycle and tick events
- Events are registered via lambdas that execute when the event fires

### Property Registration

Vanilla properties are registered in `VanillaEntityProperties` using a registry pattern:
```java
registry.put(EntityClass.class, (map) -> {
    map.put("propertyKey", new ConcreteProperty(...));
});
```

Extra properties use a factory pattern and are class-keyed for runtime type safety.

### GUI System

- Screen-based rendering using Minecraft's GUI system
- Properties are organized into categories
- Each property type has a corresponding widget for display/editing
- Localization keys defined in `Lang` class (EN/CH support)

## Key Development Notes

- Java 21 is required (set as target compatibility)
- Uses official Mojang mappings via Fabric Loom
- The mod only supports `LivingEntity` subclasses (players are excluded)
- Entity instances are NOT cached in `EntityClassInfo` to prevent client-level memory leaks
- When adding new properties, register them in the appropriate `init()` method
- The test dependencies include JavaParser, Procyon, and Vineflower for bytecode analysis

## File Structure

```
src/main/java/io/github/xienaoban/biologydictionary/
├── BiologyDictionary.java           # Main entry point (server)
├── BiologyDictionaryClient.java     # Client entry point
├── Lang.java                        # Localization keys
├── common/                          # Shared code (net, server, client, util)
├── core/                            # Core mod architecture
│   ├── EntityManager.java          # Entity metadata manager
│   ├── BiologyDictionaryItem.java  # Main item for inspecting entities
│   ├── property/                   # Property system
│   │   ├── EntityProperties.java   # Property registry & container
│   │   ├── vanilla/               # Vanilla entity properties
│   │   └── extra/                 # Custom properties
│   ├── skill/                      # Action/skill system
│   └── widget/                     # GUI components
└── gui/                            # Screen implementations
```

## Code Style & Coding Rules

- Avoid using the `this.` qualifier in code unless explicitly necessary. Follow the code style of existing code in the project.
- Do not modify any existing code without a valid justification.
- All temporary files generated during development (i.e., files that will not be committed to Git in the end) must be stored in the dedicated directory `/tmp/claude/mc/` (if in Linux). Do not scatter temporary files arbitrarily across the project. Especially pay attention to the output directory when executing `unzip`, `tar -xf`, `jar -xf` and so on (maybe you should `cd` first).
- Minecraft's first-party source code can be found in the Loom cache archive: `.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-xxx/<mc-version>-xxx/minecraft-merged-xxx-<mc-version>-xxx-sources.jar`. If you want to scan/check the Minecraft's first-party source, unzip all the source files to `/tmp/claude/mc/src` first (if they are not exist).
- Minecraft's first-party source code is obtained via decompilation. You don't need to follow its code style or local variable naming conventions — however, you should reference its class names, method names, and member variable names.
- Prioritize consulting and referencing Minecraft's official first-party code over searching for solutions online.
- If online resources for Minecraft code are unavoidable, always verify version compatibility between the retrieved materials and your target Minecraft version. Implementations of Minecraft vary significantly across different versions, which may lead to compatibility issues.
- Avoid using reflection in source code (except test code), as obfuscation is applied to field and method names in the production Minecraft runtime. Prefer using Mixin instead.
