package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.client.DiscoveryToast;
import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySources;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains an internal map of discovery records, populated via network packets.
 * Used for the DICTIONARY strategy on the client side. Entries with
 * {@code global=true} are derived global-view records; they vanish locally when
 * global sharing turns off.
 */
@ClientOnly
public final class BiologyDictionaryClientDiscoveryCache implements ClientDiscoveryCache {
    private final Map<EntityType<?>, DiscoveryRecord> cache = new ConcurrentHashMap<>();
    private final Set<EntityType<?>> acknowledgedShares = ConcurrentHashMap.newKeySet();

    /**
     * Re-pull the full effective view (own records plus the global view while
     * global sharing is on).
     */
    public void requestFullSync() {
        ClientNetManager.requestBiologyDictionaryDiscoveryFull();
    }

    /**
     * Drop the derived global view entries locally — the "sharing off" resync,
     * no server round trip needed.
     */
    public void dropGlobalRecords() {
        cache.values().removeIf(DiscoveryRecord::global);
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
        EntityType<?> entityType = EntityUtils.getEntityType(entity);
        DiscoveryRecord record = cache.get(entityType);
        if (record != null) {
            if (source == DiscoverySources.TELESCOPE) {
                notifyAlreadyDiscovered(entityType, record);
            }
            return false;
        }
        if (!source.clientCheck(ctx)) { return false; }
        ClientNetManager.requestDiscoveryIncremental(EntityUtils.getId(entity), source);
        return true;
    }

    /**
     * Telescope completion on a type someone else already discovered: a light,
     * once-per-session-per-type feedback so the no-op doesn't feel like nothing happened.
     */
    private void notifyAlreadyDiscovered(EntityType<?> entityType, DiscoveryRecord record) {
        if (!record.global()) {
            return;
        }
        if (!acknowledgedShares.add(entityType)) {
            return;
        }
        Component title = record.discovererName().isEmpty()
                ? TextUtils.translate(Lang.TEXT_ENTITY_ALREADY_DISCOVERED)
                : TextUtils.translate(Lang.TEXT_ENTITY_ALREADY_DISCOVERED_BY, record.discovererName());
        ClientUtils.getClient().gui.toastManager().addToast(new DiscoveryToast(entityType, title));
    }
}
