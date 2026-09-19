package io.github.xienaoban.biologydictionary.core.discovery.storage;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySources;
import io.github.xienaoban.biologydictionary.core.discovery.GlobalDiscoveryStats;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static final String KEY_DISCOVERER = "discoverer";
    private static final String KEY_GLOBAL = "global";
    private static final String KEY_SHARE_CHAIN = "share_chain";

    private final Map<UUID, Map<EntityType<?>, DiscoveryRecord>> data = new HashMap<>();
    private final GlobalDiscoveryStats stats = new GlobalDiscoveryStats();

    public SavedDataDiscoveryStorage() {}

    public static SavedDataDiscoveryStorage load(CompoundTag tag) {
        SavedDataDiscoveryStorage storage = new SavedDataDiscoveryStorage();
        boolean migrated = DiscoveryDataMigrator.migrate(tag);
        ListTag playersList = tag.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < playersList.size(); i++) {
            CompoundTag playerTag = playersList.getCompound(i);
            UUID uuid = playerTag.getUUID("uuid");
            CompoundTag discoveriesTag = playerTag.getCompound("discoveries");
            Map<EntityType<?>, DiscoveryRecord> entityMap = new HashMap<>();
            for (String key : discoveriesTag.getAllKeys()) {
                EntityType<?> type = EntityUtils.getEntityType(IdentifierUtils.fromStringOrNull(key));
                if (type != null) {
                    entityMap.put(type, readRecord(discoveriesTag.getCompound(key)));
                }
            }
            storage.data.put(uuid, entityMap);
        }
        storage.stats.deriveFrom(storage.data);
        if (migrated) {
            storage.setDirty();
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
            for (Map.Entry<EntityType<?>, DiscoveryRecord> discoveryEntry : entry.getValue().entrySet()) {
                discoveriesTag.put(EntityUtils.getEntityTypeIdName(discoveryEntry.getKey()), writeRecord(discoveryEntry.getValue()));
            }
            playerTag.put("discoveries", discoveriesTag);
            playersList.add(playerTag);
        }
        tag.put("players", playersList);
        return tag;
    }

    private static DiscoveryRecord readRecord(CompoundTag tag) {
        DiscoverySource source = DiscoverySources.parseSource(tag.getString(KEY_SOURCE));
        String dimStr = tag.getString(KEY_DIMENSION);
        ResourceLocation dimension = dimStr.isEmpty() ? IdentifierUtils.mc("unknown") : IdentifierUtils.fromStringOrNull(dimStr);
        String bioStr = tag.getString(KEY_BIOME);
        ResourceLocation biome = bioStr.isEmpty() ? IdentifierUtils.mc("unknown") : IdentifierUtils.fromStringOrNull(bioStr);
        int posX = tag.getInt(KEY_POS_X);
        int posY = tag.getInt(KEY_POS_Y);
        int posZ = tag.getInt(KEY_POS_Z);
        String weatherStr = tag.getString(KEY_WEATHER);
        Biome.Precipitation weather = weatherStr.isEmpty() ? Biome.Precipitation.NONE : Biome.Precipitation.valueOf(weatherStr);
        UUID entityUUID = tag.hasUUID(KEY_ENTITY_UUID) ? tag.getUUID(KEY_ENTITY_UUID) : new UUID(-1, -1);
        CompoundTag entityNbt = tag.contains(KEY_ENTITY_NBT, Tag.TAG_COMPOUND) ? tag.getCompound(KEY_ENTITY_NBT) : new CompoundTag();
        UUID discoverer = tag.hasUUID(KEY_DISCOVERER) ? tag.getUUID(KEY_DISCOVERER) : DiscoveryRecord.NO_UUID;
        boolean global = tag.getBoolean(KEY_GLOBAL);
        List<UUID> shareChain = new ArrayList<>();
        ListTag chainTag = tag.getList(KEY_SHARE_CHAIN, Tag.TAG_INT_ARRAY);
        for (int i = 0; i < chainTag.size(); i++) {
            shareChain.add(UUIDUtil.uuidFromIntArray(chainTag.getIntArray(i)));
        }
        return new DiscoveryRecord(
            discoverer, tag.getLong(KEY_TIME), tag.getLong(KEY_TICK),
            source, dimension, biome,
            new BlockPos(posX, posY, posZ), weather,
            entityUUID, entityNbt, global, shareChain
        );
    }

    private static CompoundTag writeRecord(DiscoveryRecord record) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(KEY_DISCOVERER, record.discoverer());
        tag.putLong(KEY_TIME, record.realTime());
        tag.putLong(KEY_TICK, record.gameTick());
        tag.putString(KEY_SOURCE, IdentifierUtils.toString(record.source().id()));
        tag.putString(KEY_DIMENSION, record.dimension() != null ? IdentifierUtils.toString(record.dimension()) : "");
        tag.putString(KEY_BIOME, record.biome() != null ? IdentifierUtils.toString(record.biome()) : "");
        BlockPos pos = record.position();
        tag.putInt(KEY_POS_X, pos.getX());
        tag.putInt(KEY_POS_Y, pos.getY());
        tag.putInt(KEY_POS_Z, pos.getZ());
        tag.putString(KEY_WEATHER, record.weather().name());
        tag.putUUID(KEY_ENTITY_UUID, record.entityUUID());
        tag.put(KEY_ENTITY_NBT, record.entityNbt());
        tag.putBoolean(KEY_GLOBAL, record.global());
        ListTag chainTag = new ListTag();
        for (UUID sharer : record.shareChain()) {
            chainTag.add(new IntArrayTag(UUIDUtil.uuidToIntArray(sharer)));
        }
        tag.put(KEY_SHARE_CHAIN, chainTag);
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
        Map<EntityType<?>, DiscoveryRecord> playerData = data.computeIfAbsent(playerUUID, key -> new HashMap<>());
        if (playerData.putIfAbsent(entityType, record) != null) {
            return false;
        }
        if (record.discoverer().equals(playerUUID)) {
            stats.append(entityType, record);
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
}
