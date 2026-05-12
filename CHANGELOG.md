# Changelog

All notable changes to Biology Dictionary will be documented in this file.

## Unreleased

### Added

- Added spawn property and widget
- Added cache of static properties
- Left/right-click the entity in EntityDisplayWidget to play hurt/death/ambient sound
- Added support for entity description datapacks/mods
- Added toast prompt for new discoveries
- Added support for creature discovery via telescope, kill, death and interaction events
- Added config entries for discovery sources

### Changed

- Use safe & limited YAML
- Allow setting variants in overview screen
- Changed screen colors
- Improved undiscovered entity rendering
- Changed default cost item of EntitySetSoundSkill to white wool
- Changed default cost item of MobSetNoAiSkill to totem of undying
- Moved networking logic from net thread to client/server thread
- Refactored discovery system
- Improved telescope discovery logic

### Fixed

- Fixed an EntityManager initialization failure issue
- Fixed the background flickering when going back to the previous interface
- Fixed truncated empty lines in tooltips
- Fixed spawn structure support
- Render placeholder if failed to render entity
- Fixed entity rotation jump when rendering in detail screen
- Fixed crash from spawn manager
- Speed up template parsing
- Fixed long config entry names overflowing in config screen
- Fixed bounding box display for x/y/z ≥ 10

## Previous Versions

For older versions, please check the releases page on:
- [GitHub](https://github.com/xienaoban/minecraft-biology-dictionary/releases)
- [Modrinth](https://modrinth.com/mod/biology-dictionary/changelog)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/biology-dictionary/files)
