package io.github.xienaoban.biologydictionary.core.discovery.storage;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryStorage;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * SavedData-based implementation of {@link DiscoveryStorage}.
 * Manages a single NBT file per world: {@code data/biologydictionary_discovery.dat}.
 */
public final class SavedDataDiscoveryStorage implements DiscoveryStorage {
    private static final String FILE_NAME = "biologydictionary_discovery.dat";
    private static final String KEY_PLAYERS = "players";
    private static final String KEY_UUID_MOST = "uuid_most";
    private static final String KEY_UUID_LEAST = "uuid_least";
    private static final String KEY_DISCOVERIES = "discoveries";
    private static final String KEY_DISCOVERED = "discovered";
    private static final String KEY_TIME = "first_discovery_time";
    private static final String KEY_TICK = "first_discovery_tick";

    private final Map<UUID, Map<EntityType<?>, DiscoveryRecord>> data = new HashMap<>();
    private boolean dirty;
    private final Path filePath;

    public SavedDataDiscoveryStorage(MinecraftServer server) {
        this.filePath = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(FILE_NAME);
        load();
    }

    @Override
    public DiscoveryRecord get(UUID playerUUID, EntityType<?> entityType) {
        Map<EntityType<?>, DiscoveryRecord> playerData = data.get(playerUUID);
        if (playerData == null) {
            return DiscoveryRecord.UNDISCOVERED;
        }
        return playerData.getOrDefault(entityType, DiscoveryRecord.UNDISCOVERED);
    }

    @Override
    public Map<EntityType<?>, DiscoveryRecord> getAll(UUID playerUUID) {
        Map<EntityType<?>, DiscoveryRecord> playerData = data.get(playerUUID);
        return playerData != null ? playerData : Collections.emptyMap();
    }

    @Override
    public void put(UUID playerUUID, EntityType<?> entityType, DiscoveryRecord record) {
        data.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(entityType, record);
        dirty = true;
    }

    @Override
    public void setDirty() {
        dirty = true;
    }

    public void save() {
        if (!dirty) {
            return;
        }
        try {
            Files.createDirectories(filePath.getParent());
            CompoundTag root = new CompoundTag();
            ListTag playersList = new ListTag();
            for (Map.Entry<UUID, Map<EntityType<?>, DiscoveryRecord>> entry : data.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                playerTag.putLong(KEY_UUID_MOST, entry.getKey().getMostSignificantBits());
                playerTag.putLong(KEY_UUID_LEAST, entry.getKey().getLeastSignificantBits());
                CompoundTag discoveriesTag = new CompoundTag();
                for (Map.Entry<EntityType<?>, DiscoveryRecord> discEntry : entry.getValue().entrySet()) {
                    DiscoveryRecord record = discEntry.getValue();
                    CompoundTag recordTag = new CompoundTag();
                    recordTag.putBoolean(KEY_DISCOVERED, record.discovered());
                    recordTag.putLong(KEY_TIME, record.firstDiscoveryTime());
                    recordTag.putLong(KEY_TICK, record.firstDiscoveryTick());
                    discoveriesTag.put(EntityUtils.getEntityTypeIdName(discEntry.getKey()), recordTag);
                }
                playerTag.put(KEY_DISCOVERIES, discoveriesTag);
                playersList.add(playerTag);
            }
            root.put(KEY_PLAYERS, playersList);
            try (OutputStream out = Files.newOutputStream(filePath)) {
                NbtIo.writeCompressed(root, out);
            }
            dirty = false;
        } catch (IOException e) {
            LOGGER.error("Failed to save discovery data", e);
        }
    }

    private void load() {
        if (!Files.exists(filePath)) {
            return;
        }
        try (InputStream in = Files.newInputStream(filePath)) {
            CompoundTag root = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
            ListTag playersList = root.getList(KEY_PLAYERS).orElse(new ListTag());
            for (int i = 0; i < playersList.size(); i++) {
                CompoundTag playerTag = playersList.getCompound(i).orElse(new CompoundTag());
                long most = playerTag.getLong(KEY_UUID_MOST).orElse(0L);
                long least = playerTag.getLong(KEY_UUID_LEAST).orElse(0L);
                UUID uuid = new UUID(most, least);
                CompoundTag discoveriesTag = playerTag.getCompound(KEY_DISCOVERIES).orElse(new CompoundTag());
                Map<EntityType<?>, DiscoveryRecord> playerData = new HashMap<>();
                Set<String> keys = discoveriesTag.keySet();
                for (String key : keys) {
                    CompoundTag recordTag = discoveriesTag.getCompound(key).orElse(new CompoundTag());
                    boolean discovered = recordTag.getBoolean(KEY_DISCOVERED).orElse(false);
                    long time = recordTag.getLong(KEY_TIME).orElse(0L);
                    long tick = recordTag.getLong(KEY_TICK).orElse(0L);
                    if (discovered) {
                        EntityType<?> type = EntityUtils.getEntityType(Identifier.tryParse(key));
                        if (type != null) {
                            playerData.put(type, new DiscoveryRecord(true, time, tick));
                        }
                    }
                }
                data.put(uuid, playerData);
            }
            LOGGER.info("Discovery data loaded ({} players)", data.size());
        } catch (IOException e) {
            LOGGER.error("Failed to load discovery data", e);
        }
    }
}
