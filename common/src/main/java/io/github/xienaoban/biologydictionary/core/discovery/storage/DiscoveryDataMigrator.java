package io.github.xienaoban.biologydictionary.core.discovery.storage;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Map;
import java.util.UUID;

/**
 * Load-time migration for the discovery saved data. Legacy formats are rewritten in
 * place to the current NBT layout right when the file is loaded, before the codec
 * parses it; the business layer only ever reads the current format. Callers must
 * {@code setDirty()} when {@link #migrate} reports a change so the migrated data
 * is persisted back.
 *
 * <p>Migrated layouts:
 * <ul>
 * <li>v1.0.0 wrote the source as a bare uppercase enum name; it is mapped to
 * its registry id (the whitelist only covers the closed v1.0.0 enum set);</li>
 * <li>{@code source=telescope_observe} → {@code telescope};</li>
 * <li>records without a valid {@code discoverer} get the pool owner filled in
 * (pre-feature records carry none, and the global stats derivation counts on it).</li>
 * </ul>
 */
public final class DiscoveryDataMigrator {
    /**
     * The bare enum names v1.0.0 serialized as the source (uppercase, no
     * namespace), mapped to the ids those sources gained in v1.1.0.
     */
    private static final Map<String, String> V1_0_SOURCE_IDS = Map.of(
            "ENTITY_DETAIL_SCREEN", "entity_detail_screen",
            "HIGHLIGHT", "highlight",
            "TELESCOPE_OBSERVE", "telescope_observe",
            "INTERACT", "interact",
            "KILL", "kill",
            "KILLED_BY", "killed_by"
    );

    private DiscoveryDataMigrator() {}

    /**
     * Migrate a saved-data CompoundTag in place; {@code true} if anything changed.
     * Root layout: {@code {players: [{uuid: <player-uuid>, discoveries: {<entity-type>: record}}]}}.
     */
    public static boolean migrate(CompoundTag root) {
        boolean changed = false;
        ListTag players = root.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag playerData = players.getCompound(i);
            UUID owner = playerData.hasUUID("uuid") ? playerData.getUUID("uuid") : null;
            if (owner == null) {
                continue;
            }
            CompoundTag discoveries = playerData.getCompound("discoveries");
            for (String typeKey : discoveries.getAllKeys()) {
                changed |= migrateRecord(discoveries.getCompound(typeKey), owner);
            }
        }
        return changed;
    }

    private static boolean migrateRecord(CompoundTag record, UUID owner) {
        boolean changed = false;

        UUID discoverer = readUuid(record, "discoverer");
        if (discoverer == null || discoverer.equals(DiscoveryRecord.NO_UUID)) {
            writeUuid(record, "discoverer", owner);
            changed = true;
        }

        String source = record.getString("source");
        String v10Id = V1_0_SOURCE_IDS.get(source);
        if (v10Id != null) {
            source = BiologyDictionary.MOD_ID + ":" + v10Id;
            record.putString("source", source);
            changed = true;
        }
        if (source.equals(BiologyDictionary.MOD_ID + ":telescope_observe")) {
            record.putString("source", BiologyDictionary.MOD_ID + ":telescope");
            changed = true;
        }

        return changed;
    }

    private static void writeUuid(CompoundTag tag, String key, UUID uuid) {
        tag.putIntArray(key, UUIDUtil.uuidToIntArray(uuid));
    }

    private static UUID readUuid(CompoundTag tag, String key) {
        if (!tag.contains(key, Tag.TAG_INT_ARRAY)) {
            return null;
        }
        return UUIDUtil.uuidFromIntArray(tag.getIntArray(key));
    }
}
