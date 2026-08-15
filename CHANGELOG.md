# Changelog

All notable changes to Biology Dictionary will be documented in this file.

## Release

### Added

- New players receive a Biology Dictionary item when joining the world for the first time (config `giveBookOnFirstJoin`).
- Show a centered warning when using a skill on the overview preview entity.

### Changed

- Add details to No-AI skill widget tooltip.
- Page turn markers now support more complex page turn behaviors.
- The range for opening and keeping the entity detail screen is now a server config (`entityDetailScreenRange`).
- Support attack damage & armor widgets.
- Plugin API: refactored the discovery API and added `EntityInfoApi`.

### Fixed

- Spyglass no longer shows the discovery progress bar or completion animation when observing blacklisted entities.
- Fixed a build-time client-only validation error (common code referenced `EntityDisplay`).

## Previous Versions

For older versions, please check the releases page on:
- [GitHub](https://github.com/xienaoban/minecraft-biology-dictionary/releases)
- [Modrinth](https://modrinth.com/mod/biology-dictionary/changelog)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/biology-dictionary/files)
