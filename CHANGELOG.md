# Changelog

All notable changes to Biology Dictionary will be documented in this file.

## Unreleased

### Added

- Added spawn property and widget
- Added cache of static properties
- Left/right-click the entity in EntityDisplayWidget to play hurt/death/ambient sound
- Support entity description datapacks/mods
- Added toast prompt for new discoveries
- Add support for creature discovery via telescope, kill, death and interaction events

### Changed

- Use safe & limited YAML
- Allow setting variants in overview screen
- Changed screen colors
- Improved undiscovered entity rendering
- Changed default cost item of EntitySetSoundSkill to white wool
- Changed default cost item of MobSetNoAiSkill to totem of undying
- Moved networking logic from net thread to client/server thread

### Fixed

- Fixed an EntityManager initialization failure issue
- Fixed the background flickering when going back to the previous screen
- Fixed truncated empty lines in tooltips
- Fixed spawn structure support
- Render placeholder if failed to render entity
- Fixed entity rotation jump when rendering in detail screen

## Previous Versions

For older versions, please check the releases page on:
- [GitHub](https://github.com/xienaoban/minecraft-biology-dictionary/releases)
- [Modrinth](https://modrinth.com/mod/biology-dictionary/changelog)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/biology-dictionary/files)
