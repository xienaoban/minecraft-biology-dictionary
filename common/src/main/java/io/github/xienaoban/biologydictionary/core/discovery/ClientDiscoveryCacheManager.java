package io.github.xienaoban.biologydictionary.core.discovery;

import io.github.xienaoban.biologydictionary.api.DiscoverySource;
import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.config.ConfigsUpdateCallback;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.AlwaysUnlockedClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.BiologyDictionaryClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.VanillaKillClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * Client-side manager of the discovery cache.
 * Delegates to a cache selected from the current server config.
 */
@ClientOnly
public final class ClientDiscoveryCacheManager implements ConfigsUpdateCallback {
    private volatile Configs.ServerConfigs.DiscoveryStrategyMode mode;
    private volatile ClientDiscoveryCache delegate;

    public ClientDiscoveryCacheManager() {
        onConfigsUpdate(ConfigsManager.getClient(), ConfigsManager.getServer());
    }

    @Override
    public void onConfigsUpdate(Configs.ClientConfigs clientConfigs, Configs.ServerConfigs serverConfigs) {
        Configs.ServerConfigs.DiscoveryStrategyMode newMode = serverConfigs.getDiscoveryStrategy();
        if (newMode == mode) {
            return;
        }
        mode = newMode;
        delegate = switch (newMode) {
            case ALWAYS_UNLOCKED -> new AlwaysUnlockedClientDiscoveryCache();
            case VANILLA_KILL -> new VanillaKillClientDiscoveryCache();
            case BIOLOGY_DICTIONARY -> new BiologyDictionaryClientDiscoveryCache();
        };
    }

    public ClientDiscoveryCache getDelegate() {
        return delegate;
    }

    public boolean isDiscovered(EntityType<?> entityType) {
        return delegate.isDiscovered(entityType);
    }

    public DiscoveryRecord getRecord(EntityType<?> entityType) {
        return delegate.getRecord(entityType);
    }

    public void incrementalSync(EntityType<?> entityType, DiscoveryRecord record) {
        delegate.incrementalSync(entityType, record);
    }

    public boolean onDiscoveryEvent(DiscoverySource source, LocalPlayer player, Entity entity) {
        if (!source.isEnabled()) { return false; }
        if (EntityManager.isEntityTypeBlacklisted(entity.getType())) { return false; }
        return delegate.onDiscovery(source, new DiscoverySource.ClientContext(player, entity));
    }
}
