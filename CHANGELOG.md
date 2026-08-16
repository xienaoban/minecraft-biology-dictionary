# Changelog

All notable changes to Biology Dictionary will be documented in this file.

## Release

### Added

- New players receive a Biology Dictionary item when joining the world for the first time (config `giveBookOnFirstJoin`).
- Show a centered warning when using a skill on the overview preview entity.
- Master-level librarian villagers have a chance to sell an extra Biology Dictionary without taking up a regular trade slot (config `bookItemObtainableFromMasterLibrarian`); the chance decays over game time, same as the wandering trader's.
- Survival players can freely take from and place into creatures' equipment slots in the stealing screen, controlled by new server configs `allowStealingFriendlyEntityEquipment` and `allowStealingEnemyEntityEquipment` (creative is always allowed).

### Changed

- Add details to No-AI skill widget tooltip.
- Page turn markers now support more complex page turn behaviors.
- The range for opening and keeping the entity detail screen is now a server config (`entityDetailScreenRange`).
- Support attack damage & armor widgets.
- Plugin API: refactored the discovery API and added `EntityInfoApi`.

### Fixed

- Spyglass no longer shows the discovery progress bar or completion animation when observing blacklisted entities.
- Fixed a build-time client-only validation error (common code referenced `EntityDisplay`).
- No more "Failed to create entity type minecraft:player" error when entering a world; the player is now specially handled and previewed with a mannequin (with the player's NBT injected) instead of the instance-creation-failed placeholder.

## Previous Versions

For older versions, please check the releases page on:
- [GitHub](https://github.com/xienaoban/minecraft-biology-dictionary/releases)
- [Modrinth](https://modrinth.com/mod/biology-dictionary/changelog)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/biology-dictionary/files)
