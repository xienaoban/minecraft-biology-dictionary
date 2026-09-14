# Changelog

All notable changes to Biology Dictionary will be documented in this file.

## Release

### Added

- Server-wide shared discovery progress (config `discoveryGlobalShared`, Biology Dictionary strategy only): anyone's discovery counts for everyone as a derived view — nothing extra is written to the save, and turning it off falls each player back to their own records.
- Server-wide discovery announcements for newly discovered entity types. The per-entity limit is configurable via `discoveryAnnouncementLimit` (default `5`, `-1` for unlimited, `0` to disable the server-wide broadcast); later discoverers only receive a private message. Discovered entity names show discovery details in their tooltip and can be clicked to open the entity overview. Player names can be clicked to suggest `/tell <player>` and show the player's unlocked creature count on hover.
- New `/biologydictionary overview <entity_type>` command for opening an entity overview from chat; clients without the mod receive vanilla chat feedback when the custom packet cannot be delivered. That warning links to the mod's Modrinth page.
- New keybind for stealing an entity's inventory, unbound by default.

### Changed

- Optimized packet size for discovery‑record transmission.
- Merged the entity overview reply/open-screen packets into `SendEntityOverviewPacket`; server-side overview permission and cache handling is now centralized.
- Server-sent custom packets now check whether the client can receive them before sending. Clients without the mod fall back to vanilla overlay/chat messages where applicable, and discovery/config synchronization packets are skipped.

### Fixed

- Denied entity overview requests no longer fail silently; the request path now shows the existing centered "not discovered" message.
- Discovery announcements and command feedback remain readable for clients without the mod via per-client language fallbacks (unsupported languages and the server console use the `en_us` fallback).

## Previous Versions

For older versions, please check the releases page on:
- [GitHub](https://github.com/xienaoban/minecraft-biology-dictionary/releases)
- [Modrinth](https://modrinth.com/mod/biology-dictionary/changelog)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/biology-dictionary/files)
