# Changelog

All notable changes to Biology Dictionary will be documented in this file.

## Release

### Added

- Added a server config option to limit biome/structure spawn analysis time during world startup
- Added an in-game warning when spawn analysis times out

### Changed

- Greatly improved structure spawn analysis performance by caching template pool analysis and reusing pool graph closures
- Reduced structure template parsing overhead by reading only needed NBT fields
- Refactored Biology Dictionary screen scaling to improve compatibility with other mods
- Moved demo mode out of config and behind a debug-only hidden toggle on the About screen
- Moved debug mode to a static variable

### Fixed

- Fixed beehive screen rendering.
- Worked around crashes from other mods when checking animal food items

## Previous Versions

For older versions, please check the releases page on:
- [GitHub](https://github.com/xienaoban/minecraft-biology-dictionary/releases)
- [Modrinth](https://modrinth.com/mod/biology-dictionary/changelog)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/biology-dictionary/files)
