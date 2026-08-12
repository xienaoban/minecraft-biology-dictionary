package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.api.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.api.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains an internal map of discovery records, populated via network packets.
 * Used for the DICTIONARY strategy on the client side.
 */
@ClientOnly
public final class BiologyDictionaryClientDiscoveryCache implements ClientDiscoveryCache {
    private final Map<EntityType<?>, DiscoveryRecord> cache = new ConcurrentHashMap<>();

    public BiologyDictionaryClientDiscoveryCache() {
        ClientNetManager.requestBiologyDictionaryDiscoveryFull();
    }

    @Override
    public boolean isDiscovered(EntityType<?> entityType) {
        return cache.containsKey(entityType);
    }

    @Override
    public DiscoveryRecord getRecord(EntityType<?> entityType) {
        return cache.get(entityType);
    }

    public void onFullSync(Map<EntityType<?>, DiscoveryRecord> data) {
        this.cache.clear();
        this.cache.putAll(data);
    }

    @Override
    public void incrementalSync(EntityType<?> entityType, DiscoveryRecord discoveryRecord) {
        cache.put(entityType, discoveryRecord);
    }

    @Override
    public boolean onDiscovery(DiscoverySource source, DiscoverySource.ClientContext ctx) {
        Entity entity = ctx.entity();
        if (isDiscovered(EntityUtils.getEntityType(entity))) { return false; }
        if (!source.clientCheck(ctx)) { return false; }
        ClientNetManager.requestDiscoveryIncremental(EntityUtils.getId(entity), source);
        return true;
    }
}
