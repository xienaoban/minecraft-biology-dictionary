package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains an internal map of discovery records, populated via network packets.
 * Used for the DICTIONARY strategy on the client side.
 */
@Environment(EnvType.CLIENT)
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
    public boolean onEntityDetailScreenOpened(LocalPlayer player, Entity entity) {
        EntityType<?> entityType = entity.getType();
        if (isDiscovered(entityType)) {
            return false;
        }
        DiscoveryRecord record = DiscoveryRecord.discoveredNow(player.level().getGameTime(), entity, DiscoverySource.ENTITY_DETAIL_SCREEN);
        cache.put(entityType, record);
        ClientNetManager.sendBiologyDictionaryDiscoveryIncremental(entityType, record);
        return true;
    }
}
