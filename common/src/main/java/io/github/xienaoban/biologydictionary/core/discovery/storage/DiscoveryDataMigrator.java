package io.github.xienaoban.biologydictionary.core.discovery.storage;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * Load-time migration for the discovery saved data (兼容层). Legacy formats are
 * rewritten in place to the current NBT layout right when the file is loaded,
 * before the codec parses it; the business layer only ever reads the current
 * format. Callers must {@code setDirty()} when {@link #migrate} reports a change
 * so the migrated data is persisted back.
 *
 * <p>Migrated layouts:
 * <ul>
 * <li>{@code source=telescope_observe} → {@code telescope};</li>
 * <li>records without a valid {@code discoverer} get the pool owner filled in
 * (pre-feature records carry none, and the global stats derivation counts on it).</li>
 * </ul>
 */
public final class DiscoveryDataMigrator {
    private DiscoveryDataMigrator() {}

    /**
     * Migrate a saved-data CompoundTag in place; {@code true} if anything changed.
     * Root layout: {@code {players: {<player-uuid>: {<entity-type>: record}}}}.
     */
    public static boolean migrate(CompoundTag root) {
        boolean changed = false;
        CompoundTag players = root.getCompoundOrEmpty("players");
        for (String playerKey : players.keySet()) {
            UUID owner = parseUuid(playerKey);
            if (owner == null) {
                continue;
            }
            CompoundTag playerData = players.getCompoundOrEmpty(playerKey);
            for (String typeKey : playerData.keySet()) {
                changed |= migrateRecord(playerData.getCompoundOrEmpty(typeKey), owner);
            }
        }
        return changed;
    }

    private static boolean migrateRecord(CompoundTag record, UUID owner) {
        boolean changed = false;

        String source = record.getString("source").orElse("");
        if (source.equals(BiologyDictionary.MOD_ID + ":telescope_observe")) {
            record.putString("source", BiologyDictionary.MOD_ID + ":telescope");
            changed = true;
        }

        UUID discoverer = readUuid(record, "discoverer");
        if (discoverer == null || discoverer.equals(DiscoveryRecord.NO_UUID)) {
            writeUuid(record, "discoverer", owner);
            if (!record.contains("discoverer_name")) {
                record.putString("discoverer_name", DiscoveryRecord.NO_NAME);
            }
            changed = true;
        }
        return changed;
    }

    private static void writeUuid(CompoundTag tag, String key, UUID uuid) {
        tag.putIntArray(key, UUIDUtil.uuidToIntArray(uuid));
    }

    private static UUID readUuid(CompoundTag tag, String key) {
        if (!tag.contains(key)) {
            return null;
        }
        return tag.getIntArray(key).map(UUIDUtil::uuidFromIntArray).orElse(null);
    }

    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
