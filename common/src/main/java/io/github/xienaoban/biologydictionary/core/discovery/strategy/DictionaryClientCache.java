package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryClientCache;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains an internal map of discovery records, populated via network packets.
 * Used for the DICTIONARY strategy on the client side.
 */
@Environment(EnvType.CLIENT)
public final class DictionaryClientCache implements DiscoveryClientCache {
    private final Map<Identifier, DiscoveryRecord> data = new ConcurrentHashMap<>();

    public DictionaryClientCache() {
        ClientNetManager.requestDictionaryDiscoveryFull();
    }

    @Override
    public boolean isDiscovered(Identifier entityType) {
        DiscoveryRecord record = data.get(entityType);
        return record != null && record.isDiscovered();
    }

    @Override
    public DiscoveryRecord getRecord(Identifier entityType) {
        return data.getOrDefault(entityType, DiscoveryRecord.UNDISCOVERED);
    }

    @Override
    public void onFullSync(Map<Identifier, DiscoveryRecord> data) {
        this.data.clear();
        this.data.putAll(data);
    }

    @Override
    public void onIncrementalSync(Identifier entityType, DiscoveryRecord record) {
        data.put(entityType, record);
    }

    /**
     * Get an unmodifiable view of all discovery records.
     */
    public Map<Identifier, DiscoveryRecord> getAll() {
        return Collections.unmodifiableMap(data);
    }
}
