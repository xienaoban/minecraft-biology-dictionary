![Biology Dictionary](res/header.png)

<div style="text-align: right;">

English | **[简体中文](README.zh-CN.md)**

</div>

<div style="text-align: center;">

# Biology Dictionary

**Discover, highlight, inspect, modify — your vanilla & modded mob encyclopedia**

[![Download - Modrinth](https://img.shields.io/badge/Download-Modrinth-43b581?style=for-the-badge&logo=modrinth)](https://modrinth.com/mod/biology-dictionary)
[![Download - CurseForge](https://img.shields.io/badge/Download-CurseForge-ff6b6b?style=for-the-badge&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/biology-dictionary)

![Fabric](https://img.shields.io/badge/Fabric-A99774?style=flat-square&logo=fabric)
![NeoForge](https://img.shields.io/badge/NeoForge-D7742F?style=flat-square&logo=neoforge)
![Forge](https://img.shields.io/badge/Forge-667E9F?style=flat-square&logo=neoforge)
![MC 1.21.11](https://img.shields.io/badge/MC-1.21.11%20%7C%201.21.1%20%7C%201.20.1-59A922?style=flat-square&logo=minecraft)

</div>

---

## Sound familiar?

| Problem | Solution |
|---------|----------|
| Don't know which mobs you're still missing | Check your discovery progress in the encyclopedia |
| Want spawn biomes, drops, food, speed, hitbox... all at once | View all base properties on a single page |
| Curious about horse, panda, or villager variants | Browse all variants (and change them in Creative) |
| Baby mobs are too adorable to grow up | Lock growth, keep them babies forever |
| Dozens of chickens in a cooker are deafening | One-click mute — offspring inherit it too |
| Can't find the villager's job site in your trading hall | One-click locate, no more blind digging |
| Villagers keep crafting bread, breaking your auto-farm | Steal their inventory, fill with junk, stop wheat pickup |
| Bees flew off — which hive did they go to? | View hive honey levels and bee counts, track them home |
| Cross-dimension mob transport is a nightmare | Lock or force nether portal teleportation |
| Want NPCs in your survival builds | Disable AI + set invulnerable, done |

---

## Screenshots

|                                             |                                             |
|:-------------------------------------------:|:-------------------------------------------:|
| ![screenshot 1](res/screenshot_en_us_1.jpg) | ![screenshot 2](res/screenshot_en_us_2.jpg) |
| ![screenshot 3](res/screenshot_en_us_3.jpg) | ![screenshot 4](res/screenshot_en_us_4.jpg) |

<details>
<summary>Click to expand more screenshots</summary>

|                                             |                                             |
|:-------------------------------------------:|:-------------------------------------------:|
| ![screenshot 5](res/screenshot_en_us_5.jpg) | ![screenshot ](res/screenshot_en_us_6.jpg)  |
| ![screenshot 7](res/screenshot_en_us_7.jpg) | ![screenshot 8](res/screenshot_en_us_8.jpg) |
| ![screenshot 9](res/screenshot_en_us_9.jpg) |                                             |

</details>

---

## Features

- **No new blocks or entities. Vanilla-friendly and safe to uninstall anytime.**
- **Full support for vanilla and modded mobs alike.**

### Inspection

- **Mob Discovery** — Track your encyclopedia progress; discover mobs by viewing details, scanning with the telescope, highlighting, killing, interacting, and more
- **Entity Highlight** — Highlight a specific mob type within 100 m to quickly track down rare spawns
- **Health & Effects** — Real-time health bar and active status effects
- **Speed & Jump** — Precise movement speed (m/s) and jump strength (m)
- **Loot Table** — Full drop table for any mob
- **Spawn Info** — Spawn biomes and structures (modded content supported)
- **Hitbox** — Exact bounding box dimensions
- **Variants** — Villager outfits, horse markings, cow biome variants, panda genes, and more
- **Food** — Items that tempt or breed the mob
- **Breeding Status** — Cooldown timer and love status
- **Villager Job Site** — Locate the workstation with one click
- **Villager Schedule & Restock** — Daily schedule and restock count
- **Beehive** — Honey level, bee count, hive location
- **And more** — Dolphin moisture, screaming goat check, pet owner info, etc.

### Modification

All modifications cost in-game resources (XP, items, etc.) to keep things balanced.

- **Lock Growth** — Freeze baby mobs permanently
- **Prevent Breeding** — Block mobs from entering love mode
- **Portal Control** — Prevent or force nether portal teleportation
- **Remove AI** — Disable AI for static NPCs or decoration
- **Force Persistence** — Stop mobs from despawning
- **Set Invulnerable** — Make a mob immune to all damage
- **Mute** — Silence mob sounds; offspring inherit the trait
- **Inventory** — View and take items from villager inventories
- **Change Variants** — Swap villager outfits, horse markings, cow variants, panda genes, etc.
- **Force Restock** — Instantly refresh villager trades
- **Retain Trader** — Keep a wandering trader from despawning
- **Gift Pet** — Transfer a tamed mob to another player
- **Spawn Egg** — Obtain the corresponding spawn egg (Creative only)
- **And more** — Force sheep to eat grass, modify mob variants (Creative only), etc.

### Controls

Open the Biology Dictionary by:
1. Right-clicking the book item while holding it
2. Pressing the hotkey (default `` ` ``) — requires the book in inventory (Survival), or is always available (Creative / configured)

The screen you get depends on where you're looking:

| Looking at | Opens |
|:----------:|:-----:|
| Block / Air | Main menu |
| Entity | Entity detail |
| Beehive | Beehive info |
| Straight up | Main menu (always) |
| Straight down | Your own stats |

---

### Obtaining

The book is a plain `minecraft:writable_book` with embedded NBT — no custom items, fully vanilla-compatible. In Creative mode (or with hotkey configured for Survival), you don't even need it.

#### Creative

Find it at the end of the **Tools & Utilities** tab.

#### Survival

Buy from **Wandering Traders**. Sell chance starts at 100% and gradually drops to 20% over time, without taking up any vanilla trade slots.

> Easy to find early when you're broke, impossible to find later when you're rich (cue evil laugh)~
>
> Kidding — it takes about **2 real-time hours** (144 in-game days) to hit 20%. You'll be fine.

Pack authors can disable trader sales in the config and add their own recipes instead.

---

## Dependencies

| Loader | Required |
|--------|----------|
| Fabric | Fabric API, Cloth Config |
| NeoForge / Forge | Architectury API, Cloth Config |

---

## About

- **Vanilla-Friendly** — No new blocks or entities. The book reverts to a normal writable book on uninstall; reinstall to restore it.
- **Mod-Friendly** — Compatible with virtually all modded mobs, with extension support for mod developers.
- **Balanced** — Every modification has a resource cost to respect game balance.
- A complete rewrite of [Bole](https://github.com/xienaoban/minecraft-bole).

---

<div style="text-align: center;">

[![License](https://img.shields.io/badge/License-LGPL_3-blue?style=for-the-badge)](LICENSE)

</div>
