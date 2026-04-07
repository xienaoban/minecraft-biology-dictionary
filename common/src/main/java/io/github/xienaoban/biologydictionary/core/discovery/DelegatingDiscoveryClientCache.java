package io.github.xienaoban.biologydictionary.core.discovery;

import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.AlwaysUnlockedClientCache;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.DictionaryClientCache;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.KillBasedClientCache;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import net.minecraft.resources.Identifier;

import java.util.Map;

/**
 * A {@link DiscoveryClientCache} that delegates to another cache instance.
 * Allows the delegate to be swapped based on the current server config.
 */
public final class DelegatingDiscoveryClientCache implements DiscoveryClientCache {
    private volatile DiscoveryClientCache delegate;

    public DelegatingDiscoveryClientCache() {
        update(ConfigsManager.getServer(), Map.of());
    }

    public void update(Configs.ServerConfigs serverConfigs, Map<Identifier, DiscoveryRecord> data) {
        delegate = switch (serverConfigs.getDiscoveryStrategy()) {
            case ALWAYS_UNLOCKED -> AlwaysUnlockedClientCache.INSTANCE;
            case VANILLA_KILL -> KillBasedClientCache.INSTANCE;
            case DICTIONARY -> new DictionaryClientCache();
        };

        onFullSync(data);
    }

    @Override
    public boolean isDiscovered(Identifier entityType) {
        if (PlayerUtils.isCreative(ClientUtils.getClientPlayer())) { return true; }
        return delegate.isDiscovered(entityType);
    }

    @Override
    public DiscoveryRecord getRecord(Identifier entityType) {
        return delegate.getRecord(entityType);
    }

    @Override
    public void onFullSync(Map<Identifier, DiscoveryRecord> data) {
        delegate.onFullSync(data);
    }

    @Override
    public void onIncrementalSync(Identifier entityType, DiscoveryRecord record) {
        delegate.onIncrementalSync(entityType, record);
    }
}
