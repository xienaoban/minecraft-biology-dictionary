# Changelog

All notable changes to Biology Dictionary will be documented in this file.

## Release

### Added

- Added a multi-selection mode for selecting multiple entity types on the home screen
- Added a toggle to show only discovered entities, with a configurable default and session-persistent state
- Added a selection count display while in selection mode
- Added a blacklist feature: the server config `entityTypeBlacklist` excludes entity types from the dictionary, and selection mode can add the selected entities to it
- Added a confirmation dialog before applying the blacklist, including a warning that the blacklist is server-side when playing on a remote server
- Added a new default "Boss" tag to the entity catalog, populated from the `c:bosses` convention tag
- Added spawn egg lore display in the entity description widget, reading `lore.<namespace>.<path>` translation keys
- Added a plugin API for third-party mods to register custom skills, extra entity properties, entity display order, client-side property widgets, and discovery sources via `@BiologyDictionaryPlugin` / `@BiologyDictionaryClientPlugin`
- Added public discovery API facades (`ServerDiscoveryApi` / `ClientDiscoveryApi`) for querying and recording discoveries

### Changed

- Refactored the discovery system: discovery sources are now pluggable and registered through a central `DiscoverySources` registry; the client discovery cache was consolidated into `ClientDiscoveryCacheManager`
- Refactored `EntityManager` around per-entry creation-failure tracking (`EntityDictionaryEntry`); introduced the `EntityDisplay` helper and removed the placeholder fallback renderer
- Reorganized plugin interfaces into the `api.plugin` package
- Improved entity display widget rendering with an inner border
- Reordered server config entries

### Fixed

- Fixed the discovery progress tooltip not appearing after turning pages on the home screen
- Fixed session teardown when leaving a world so no screen renders against a torn-down session on the render thread

## Previous Versions

For older versions, please check the releases page on:
- [GitHub](https://github.com/xienaoban/minecraft-biology-dictionary/releases)
- [Modrinth](https://modrinth.com/mod/biology-dictionary/changelog)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/biology-dictionary/files)
