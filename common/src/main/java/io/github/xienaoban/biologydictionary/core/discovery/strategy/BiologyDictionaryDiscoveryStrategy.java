package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryStrategy;
import io.github.xienaoban.biologydictionary.core.discovery.storage.SavedDataDiscoveryStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

/**
 * Entity is discovered when the player interacts with it via the mod
 * (highlight skill or entity overview screen).
 * Uses mod-managed persistent data (SavedDataDiscoveryStorage).
 */
public final class BiologyDictionaryDiscoveryStrategy implements DiscoveryStrategy {
    private final SavedDataDiscoveryStorage storage;

    public BiologyDictionaryDiscoveryStrategy(MinecraftServer server) {
        this.storage = new SavedDataDiscoveryStorage(server);
    }

    @Override
    public boolean isDiscovered(ServerPlayer player, EntityType<?> entityType) {
        return storage.get(player.getUUID(), entityType).discovered();
    }

    @Override
    public void save() {
        storage.save();
    }

    public Map<EntityType<?>, DiscoveryRecord> getAllRecords(ServerPlayer player) {
        return storage.getAll(player.getUUID());
    }

    /**
     * Store the discovery record from client. Does nothing if already discovered.
     */
    public void markDiscovered(ServerPlayer player, EntityType<?> entityType, DiscoveryRecord record) {
        if (storage.get(player.getUUID(), entityType).discovered()) {
            return;
        }
        storage.put(player.getUUID(), entityType, record);
    }
}
