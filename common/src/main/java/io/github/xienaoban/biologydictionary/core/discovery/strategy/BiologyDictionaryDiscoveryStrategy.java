package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryStrategy;
import io.github.xienaoban.biologydictionary.core.discovery.storage.SavedDataDiscoveryStorage;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/**
 * Entity is discovered when the player interacts with it via the mod
 * (highlight skill or entity overview screen).
 * Uses mod-managed persistent data (SavedDataDiscoveryStorage).
 */
public final class BiologyDictionaryDiscoveryStrategy implements DiscoveryStrategy {
    private final SavedDataDiscoveryStorage storage;

    public BiologyDictionaryDiscoveryStrategy() {
        this.storage = new SavedDataDiscoveryStorage(ServerWorldSession.get().getServer());
    }

    @Override
    public boolean isDiscovered(Identifier entityType, ServerPlayer player) {
        return storage.get(player.getUUID(), entityType).discovered();
    }

    @Override
    public DiscoveryRecord onEntityViewedInDictionary(ServerPlayer player, Identifier entityType) {
        return markDiscovered(player, entityType);
    }

    @Override
    public DiscoveryRecord onEntityHighlightedWithResult(ServerPlayer player, Identifier entityType) {
        return markDiscovered(player, entityType);
    }

    @Override
    public void save() {
        storage.save();
    }

    public Map<Identifier, DiscoveryRecord> getAllRecords(ServerPlayer player) {
        return storage.getAll(player.getUUID());
    }

    /**
     * Mark the entity as discovered for the player.
     * @return the discovery record if this is a new discovery, or null if already discovered.
     */
    private DiscoveryRecord markDiscovered(ServerPlayer player, Identifier entityType) {
        DiscoveryRecord record = storage.get(player.getUUID(), entityType);
        if (!record.discovered()) {
            DiscoveryRecord newRecord = DiscoveryRecord.discoveredNow(player.level().getGameTime());
            storage.put(player.getUUID(), entityType, newRecord);
            return newRecord;
        }
        return null;
    }
}
