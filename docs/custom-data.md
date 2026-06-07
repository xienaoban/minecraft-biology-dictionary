# Custom Data

## Custom Entity Descriptions (Resource Pack)

Biology Dictionary supports per-entity descriptions displayed on entity detail screens. These descriptions are defined via Minecraft's standard **language file** system — add entries to your mod's lang JSON.

> **Note:** Language files are part of resource packs, which are **client-side** only. Place them in your client-side resource pack.

### Translation Keys

The mod checks the following keys in order and uses the first one found:

| Priority | Key pattern | Example |
|:--------:|:------------|:--------|
| 1 | `entity.<namespace>.<path>.description` | `entity.minecraft.cow.description` |
| 2 | `entity.<namespace>.<path>.desc` | `entity.minecraft.cow.desc` |
| 3 | `lore.<namespace>.<path>` | `lore.minecraft.cow` |

### File Location

Place your translation entries in the standard lang file:

```
assets/<namespace>/lang/<locale>.json
```

### Example

In `assets/mymod/lang/en_us.json`:

```json
{
  "entity.mymod.custom_mob.description": "A mysterious creature found deep underground."
}
```

### Behavior

- If no matching key exists, the description widget is hidden by default. This can be changed in the client config (`Hide Entity Description Widget If Not Found`).
- Descriptions support Minecraft's text formatting (color codes, translatable components, etc.) since they are rendered as standard `Component`s.

---

## Custom Spawn Descriptions (Datapack)

Biology Dictionary reads spawn biome and structure information from Minecraft's built-in registry data. For most vanilla and modded mobs this works out of the box, but some mobs spawn in non-standard ways (e.g. bees near flowers, wardens in ancient cities) that the registry alone cannot capture.

To fill these gaps, the mod supports **datapack overrides** at:

> **Note:** These files are part of datapacks, which are **server-side** only. Place them in your server-side datapack.

```
data/<namespace>/biologydictionary/entity_spawn/<entity_namespace>.<entity_path>.json
```

- `<namespace>` — your datapack/mod namespace (e.g. `mymod`)
- `<entity_namespace>.<entity_path>` — the full entity ID with `.` as separator (e.g. `minecraft.bee`, `mymod.custom_mob`)

### JSON Format

```json
{
  "biomes": {
    "add": ["<biome_id>", "#<biome_tag>"],
    "remove": ["<biome_id>"],
    "overwrite": ["<biome_id>"]
  },
  "structures": {
    "add": ["<structure_id>"],
    "remove": ["<structure_id>"],
    "overwrite": ["<structure_id>"]
  }
}
```

Both `biomes` and `structures` are optional. You can include only one of them.

### Operations

| Operation | Description |
|:---------:|:------------|
| `add` | Add entries to the existing list. Duplicates are skipped with a warning. |
| `remove` | Remove entries from the existing list. Non-existent entries are skipped with a warning. |
| `overwrite` | Replace the entire list. Cannot be used together with `add`/`remove`. |

> **Note:** If `add`/`remove` and `overwrite` are used together, only `overwrite` takes effect (with a warning). When multiple datapacks use `overwrite` for the same entity, only the highest-priority one effectively applies — therefore `overwrite` is not recommended in multi-datapack scenarios.

### Identifiers

- **Direct ID**: `"minecraft:flower_forest"`
- **Tag reference** (prefix with `#`): `"#minecraft:is_forest"` — resolves to all biomes/structures in that tag

### Examples

Add spawn biomes for bees:

```json
{
  "biomes": {
    "add": ["minecraft:flower_forest", "minecraft:meadow", "minecraft:cherry_grove", "minecraft:forest", "minecraft:birch_forest", "minecraft:old_growth_birch_forest"]
  }
}
```

Add structure spawn for wardens:

```json
{
  "structures": {
    "add": ["minecraft:ancient_city"]
  }
}
```

### Validation

- Unknown entity types in the file path are skipped with a warning.
- Unknown biome/structure IDs are skipped with a warning.
- Unknown tags are skipped with a warning.
- Malformed JSON logs an error but does not crash.
