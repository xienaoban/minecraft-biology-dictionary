package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.client.player.LocalPlayer;
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
    public boolean onEntityDetailScreenOpened(LocalPlayer player, Entity entity) {
        if (isDiscovered(EntityUtils.getEntityType(entity))) { return false; }
        ClientNetManager.requestDiscoveryIncremental(EntityUtils.getId(entity), DiscoverySource.ENTITY_DETAIL_SCREEN);
        return true;
    }

    @Override
    public boolean onEntityHighlighted(LocalPlayer player, Entity entity) {
        if (isDiscovered(EntityUtils.getEntityType(entity))) { return false; }
        ClientNetManager.requestDiscoveryIncremental(EntityUtils.getId(entity), DiscoverySource.HIGHLIGHT);
        return true;
    }

    @Override
    public boolean onEntityObservedWithTelescope(LocalPlayer player, Entity entity) {
        if (isDiscovered(EntityUtils.getEntityType(entity))) { return false; }
        ClientNetManager.requestDiscoveryIncremental(EntityUtils.getId(entity), DiscoverySource.TELESCOPE_OBSERVE);
        return true;
    }
}
