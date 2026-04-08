package io.github.xienaoban.biologydictionary.core.discovery;

import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.config.ConfigsUpdateCallback;
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
public final class DelegatingDiscoveryClientCache implements DiscoveryClientCache, ConfigsUpdateCallback {
    private volatile Configs.ServerConfigs.DiscoveryStrategyMode mode;
    private volatile DiscoveryClientCache delegate;

    public DelegatingDiscoveryClientCache() {
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
            case ALWAYS_UNLOCKED -> new AlwaysUnlockedClientCache();
            case VANILLA_KILL -> new KillBasedClientCache();
            case DICTIONARY -> new DictionaryClientCache();
        };
    }

    public DiscoveryClientCache getDelegate() {
        return delegate;
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
