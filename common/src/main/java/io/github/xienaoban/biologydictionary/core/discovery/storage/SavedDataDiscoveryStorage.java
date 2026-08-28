package io.github.xienaoban.biologydictionary.core.discovery.storage;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecordSerializer;
import io.github.xienaoban.biologydictionary.core.discovery.GlobalDiscoveryStats;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Persisted per-world discovery data using MC's SavedData framework.
 * File: {@code data/biologydictionary_discovery.dat}
 */
public final class SavedDataDiscoveryStorage extends SavedData {
    private record PackedWithMigration(Packed packed, boolean migrated) {}

    /**
     * Decodes legacy layouts by migrating the CompoundTag in place first
     * (see {@link DiscoveryDataMigrator}); encoding passes straight through.
     */
    private static final Codec<PackedWithMigration> MIGRATING_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<PackedWithMigration, T>> decode(DynamicOps<T> ops, T input) {
            boolean migrated = input instanceof CompoundTag tag && DiscoveryDataMigrator.migrate(tag);
            return Packed.CODEC.decode(ops, input)
                    .map(pair -> new Pair<>(new PackedWithMigration(pair.getFirst(), migrated), pair.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(PackedWithMigration input, DynamicOps<T> ops, T prefix) {
            return Packed.CODEC.encode(input.packed(), ops, prefix);
        }
    };

    public static final SavedDataType<SavedDataDiscoveryStorage> TYPE = new SavedDataType<>(
            IdentifierUtils.bd("discovery"),
            SavedDataDiscoveryStorage::new,
            MIGRATING_CODEC.xmap(
                    paired -> {
                        SavedDataDiscoveryStorage storage = new SavedDataDiscoveryStorage(paired.packed());
                        if (paired.migrated()) {
                            storage.setDirty();
                        }
                        return storage;
                    },
                    storage -> new PackedWithMigration(storage.getPacked(), false)),
            DataFixTypes.SAVED_DATA_STOPWATCHES
    );

    private final Map<UUID, Map<EntityType<?>, DiscoveryRecord>> data = new HashMap<>();
    private final GlobalDiscoveryStats stats = new GlobalDiscoveryStats();

    public SavedDataDiscoveryStorage() {}

    private SavedDataDiscoveryStorage(Packed packed) {
        for (var entry : packed.players().entrySet()) {
            Map<EntityType<?>, DiscoveryRecord> entityMap = new HashMap<>();
            for (var discoveryEntry : entry.getValue().entrySet()) {
                EntityType<?> type = EntityUtils.getEntityType(discoveryEntry.getKey());
                if (type != null) {
                    entityMap.put(type, discoveryEntry.getValue());
                }
            }
            data.put(entry.getKey(), entityMap);
        }
        stats.deriveFrom(data);
    }

    private Packed getPacked() {
        Map<UUID, Map<Identifier, DiscoveryRecord>> result = new HashMap<>();
        for (var entry : data.entrySet()) {
            Map<Identifier, DiscoveryRecord> entityMap = new HashMap<>();
            for (var discoveryEntry : entry.getValue().entrySet()) {
                entityMap.put(EntityUtils.getEntityTypeId(discoveryEntry.getKey()), discoveryEntry.getValue());
            }
            result.put(entry.getKey(), entityMap);
        }
        return new Packed(result);
    }

    public boolean isDiscovered(UUID playerUUID, EntityType<?> entityType) {
        Map<EntityType<?>, DiscoveryRecord> playerData = data.get(playerUUID);
        return playerData != null && playerData.containsKey(entityType);
    }

    public Map<EntityType<?>, DiscoveryRecord> getAll(UUID playerUUID) {
        Map<EntityType<?>, DiscoveryRecord> playerData = data.get(playerUUID);
        return playerData != null ? playerData : Map.of();
    }

    public DiscoveryRecord get(UUID playerUUID, EntityType<?> entityType) {
        Map<EntityType<?>, DiscoveryRecord> playerData = data.get(playerUUID);
        return playerData != null ? playerData.get(entityType) : null;
    }

    public boolean put(UUID playerUUID, EntityType<?> entityType, DiscoveryRecord record) {
        Map<EntityType<?>, DiscoveryRecord> playerData = data.computeIfAbsent(playerUUID, key -> new HashMap<>());
        if (playerData.putIfAbsent(entityType, record) != null) {
            return false;
        }
        if (record.discoverer().equals(playerUUID)) {
            stats.append(entityType, playerUUID, record.discovererName(), record);
        }
        setDirty();
        return true;
    }

    /**
     * UUIDs of all players that have a pool.
     */
    public Set<UUID> players() {
        return data.keySet();
    }

    /**
     * Resident global discovery statistics, derived at load and maintained by {@link #put}
     * (genuine discoveries only: discoverer must be the pool owner).
     */
    public GlobalDiscoveryStats stats() {
        return stats;
    }

    record Packed(Map<UUID, Map<Identifier, DiscoveryRecord>> players) {
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(UUIDUtil.STRING_CODEC,
                                Codec.unboundedMap(IdentifierUtils.codec(), DiscoveryRecordSerializer.CODEC)
                        ).fieldOf("players").forGetter(Packed::players)
        ).apply(instance, Packed::new));
    }
}
