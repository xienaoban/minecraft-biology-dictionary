package io.github.xienaoban.biologydictionary.core.discovery.storage;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persisted per-world discovery data using MC's SavedData framework.
 * File: {@code data/biologydictionary_discovery.dat}
 */
public final class SavedDataDiscoveryStorage extends SavedData {
    private static final String KEY_SOURCE = "source";
    private static final String KEY_DIMENSION = "dimension";
    private static final String KEY_BIOME = "biome";
    private static final String KEY_POS_X = "pos_x";
    private static final String KEY_POS_Y = "pos_y";
    private static final String KEY_POS_Z = "pos_z";
    private static final String KEY_WEATHER = "weather";
    private static final String KEY_ENTITY_UUID = "entity_uuid";
    private static final String KEY_ENTITY_NBT = "entity_nbt";
    private static final String KEY_TIME = "time";
    private static final String KEY_TICK = "tick";

    private final Map<UUID, Map<EntityType<?>, DiscoveryRecord>> data = new HashMap<>();

    public SavedDataDiscoveryStorage() {}

    public static SavedDataDiscoveryStorage load(CompoundTag tag) {
        SavedDataDiscoveryStorage storage = new SavedDataDiscoveryStorage();
        ListTag playersList = tag.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < playersList.size(); i++) {
            CompoundTag playerTag = playersList.getCompound(i);
            UUID uuid = playerTag.getUUID("uuid");
            CompoundTag discoveriesTag = playerTag.getCompound("discoveries");
            Map<EntityType<?>, DiscoveryRecord> entityMap = new HashMap<>();
            for (String key : discoveriesTag.getAllKeys()) {
                EntityType<?> type = EntityUtils.getEntityType(ResourceLocation.tryParse(key));
                if (type != null) {
                    entityMap.put(type, readRecord(discoveriesTag.getCompound(key)));
                }
            }
            storage.data.put(uuid, entityMap);
        }
        return storage;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag playersList = new ListTag();
        for (Map.Entry<UUID, Map<EntityType<?>, DiscoveryRecord>> entry : data.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("uuid", entry.getKey());
            CompoundTag discoveriesTag = new CompoundTag();
            for (Map.Entry<EntityType<?>, DiscoveryRecord> discEntry : entry.getValue().entrySet()) {
                discoveriesTag.put(EntityUtils.getEntityTypeIdName(discEntry.getKey()), writeRecord(discEntry.getValue()));
            }
            playerTag.put("discoveries", discoveriesTag);
            playersList.add(playerTag);
        }
        tag.put("players", playersList);
        return tag;
    }

    private static DiscoveryRecord readRecord(CompoundTag tag) {
        DiscoverySource source = tag.contains(KEY_SOURCE) ? DiscoverySource.valueOf(tag.getString(KEY_SOURCE)) : DiscoverySource.UNKNOWN;
        String dimStr = tag.getString(KEY_DIMENSION);
        ResourceLocation dimension = dimStr.isEmpty() ? new ResourceLocation("unknown") : ResourceLocation.tryParse(dimStr);
        String bioStr = tag.getString(KEY_BIOME);
        ResourceLocation biome = bioStr.isEmpty() ? new ResourceLocation("unknown") : ResourceLocation.tryParse(bioStr);
        int posX = tag.getInt(KEY_POS_X);
        int posY = tag.getInt(KEY_POS_Y);
        int posZ = tag.getInt(KEY_POS_Z);
        String weatherStr = tag.getString(KEY_WEATHER);
        Biome.Precipitation weather = weatherStr.isEmpty() ? Biome.Precipitation.NONE : Biome.Precipitation.valueOf(weatherStr);
        UUID entityUUID = tag.hasUUID(KEY_ENTITY_UUID) ? tag.getUUID(KEY_ENTITY_UUID) : new UUID(-1, -1);
        CompoundTag entityNbt = tag.contains(KEY_ENTITY_NBT, Tag.TAG_COMPOUND) ? tag.getCompound(KEY_ENTITY_NBT) : new CompoundTag();
        return new DiscoveryRecord(
            tag.getLong(KEY_TIME), tag.getLong(KEY_TICK),
            source, dimension, biome,
            new BlockPos(posX, posY, posZ), weather,
            entityUUID, entityNbt
        );
    }

    private static CompoundTag writeRecord(DiscoveryRecord record) {
        CompoundTag tag = new CompoundTag();
        tag.putLong(KEY_TIME, record.firstDiscoveryTime());
        tag.putLong(KEY_TICK, record.firstDiscoveryTick());
        tag.putString(KEY_SOURCE, record.source().name());
        tag.putString(KEY_DIMENSION, record.dimension() != null ? record.dimension().toString() : "");
        tag.putString(KEY_BIOME, record.biome() != null ? record.biome().toString() : "");
        BlockPos pos = record.position();
        tag.putInt(KEY_POS_X, pos.getX());
        tag.putInt(KEY_POS_Y, pos.getY());
        tag.putInt(KEY_POS_Z, pos.getZ());
        tag.putString(KEY_WEATHER, record.weather().name());
        tag.putUUID(KEY_ENTITY_UUID, record.entityUUID());
        tag.put(KEY_ENTITY_NBT, record.entityNbt());
        return tag;
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
        Map<EntityType<?>, DiscoveryRecord> playerData = data.computeIfAbsent(playerUUID, k -> new HashMap<>());
        if (playerData.putIfAbsent(entityType, record) != null) {
            return false;
        }
        setDirty();
        return true;
    }
}
