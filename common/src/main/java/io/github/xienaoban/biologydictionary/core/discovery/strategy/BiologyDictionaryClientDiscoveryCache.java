package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains an internal map of discovery records, populated via network packets.
 * Used for the DICTIONARY strategy on the client side.
 */
@Environment(EnvType.CLIENT)
public final class BiologyDictionaryClientDiscoveryCache implements ClientDiscoveryCache {
    private final Map<EntityType<?>, DiscoveryRecord> data = new ConcurrentHashMap<>();

    public BiologyDictionaryClientDiscoveryCache() {
        ClientNetManager.requestDictionaryDiscoveryFull();
    }

    @Override
    public boolean isDiscovered(EntityType<?> entityType) {
        DiscoveryRecord record = data.get(entityType);
        return record != null && record.discovered();
    }

    @Override
    public DiscoveryRecord getRecord(EntityType<?> entityType) {
        return data.getOrDefault(entityType, DiscoveryRecord.UNDISCOVERED);
    }

    public void onFullSync(Map<EntityType<?>, DiscoveryRecord> data) {
        this.data.clear();
        this.data.putAll(data);
    }

    public void onIncrementalSync(EntityType<?> entityType, DiscoveryRecord record) {
        data.put(entityType, record);
    }
}
