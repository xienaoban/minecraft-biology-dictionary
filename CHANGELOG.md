# Changelog

## Added

- Added tracking for entity types that fail during creation.
- Added an About screen widget listing failed entity creation types.
- Added a server config option for the far highlight skill radius.
- Added always-on telescope ranging in debug mode for both entities and blocks.

## Changed

- Improved mod compatibility by skipping entity types that fail to be created, preventing unsupported entities from breaking the dictionary screen.
- Use `EntitySpawnReason.LOAD` when creating preview entities.
- Centralized display setup for preview entities.
- Improve EntitySpawnManager.
- Refactored Biology Dictionary screen rendering internals.

## Fixed

- Fixed display issues for water animal previews.
- Fixed missing display setup in entity overview, variant, beehive, and entity display widgets.
- Fixed the owner widget being created for entities without the vanilla `Owner` property, including vexes.
- Fixed individual property widget creation failures preventing dictionary screens from opening.

## Previous Versions

For older versions, please check the releases page on:
- [GitHub](https://github.com/xienaoban/minecraft-biology-dictionary/releases)
- [Modrinth](https://modrinth.com/mod/biology-dictionary/changelog)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/biology-dictionary/files)
