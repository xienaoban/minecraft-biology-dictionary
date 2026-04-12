# Changelog

All notable changes to Biology Dictionary will be documented in this file.

## Unreleased

### Added

- Added entity discovery system: players can now unlock the entity overview screen upon discovering corresponding entities
- Added `biologydictionary config reload` command to reload server configs from disk
- Added entity spawn manager (no widget yet)
- Implement rendering silhouettes of entities

### Changed

- Refactored data structure lifecycles: added client/server/common world sessions to manage them
- Refactored the configuration system: caches and managers are now automatically updated following config modifications
- Entities are discovered when killed by default

### Fixed

- Fixed an EntityGiftPetSkill issue: NullPointerException thrown if owner is not online
- Fixed centered text flickering in Biology Dictionary screens after vanishing
- Fixed quick move on stealing screen
- Tooltip now supports newline

## Previous Versions

For older versions, please check the releases page on:
- [GitHub](https://github.com/xienaoban/minecraft-biology-dictionary/releases)
- [Modrinth](https://modrinth.com/mod/biology-dictionary/changelog)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/biology-dictionary/files)
