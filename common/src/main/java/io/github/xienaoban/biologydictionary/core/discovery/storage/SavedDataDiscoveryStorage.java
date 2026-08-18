package io.github.xienaoban.biologydictionary.core.discovery.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecordSerializer;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persisted per-world discovery data using MC's SavedData framework.
 * File: {@code data/biologydictionary_discovery.dat}
 */
public final class SavedDataDiscoveryStorage extends SavedData {
    public static final SavedDataType<SavedDataDiscoveryStorage> TYPE = new SavedDataType<>(
            IdentifierUtils.bd("discovery"),
            SavedDataDiscoveryStorage::new,
            SavedDataDiscoveryStorage.Packed.CODEC.xmap(
                    SavedDataDiscoveryStorage::new, SavedDataDiscoveryStorage::getPacked),
            DataFixTypes.SAVED_DATA_STOPWATCHES
    );

    private final Map<UUID, Map<EntityType<?>, DiscoveryRecord>> data = new HashMap<>();

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
        setDirty();
        return true;
    }

    record Packed(Map<UUID, Map<Identifier, DiscoveryRecord>> players) {
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(UUIDUtil.STRING_CODEC,
                                Codec.unboundedMap(IdentifierUtils.codec(), DiscoveryRecordSerializer.CODEC)
                        ).fieldOf("players").forGetter(Packed::players)
        ).apply(instance, Packed::new));
    }
}
