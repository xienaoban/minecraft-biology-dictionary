package io.github.xienaoban.biologydictionary.core.discovery;

import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.config.ConfigsUpdateCallback;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.AlwaysUnlockedClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.BiologyDictionaryClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.VanillaKillClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import net.minecraft.world.entity.EntityType;

/**
 * A {@link ClientDiscoveryCache} that delegates to another cache instance.
 * Allows the delegate to be swapped based on the current server config.
 */
public final class DelegatingClientDiscoveryCache implements ClientDiscoveryCache, ConfigsUpdateCallback {
    private volatile Configs.ServerConfigs.DiscoveryStrategyMode mode;
    private volatile ClientDiscoveryCache delegate;

    public DelegatingClientDiscoveryCache() {
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

    @Override
    public boolean isDiscovered(EntityType<?> entityType) {
        if (PlayerUtils.isCreative(ClientUtils.getClientPlayer())) { return true; }
        return delegate.isDiscovered(entityType);
    }

    @Override
    public DiscoveryRecord getRecord(EntityType<?> entityType) {
        return delegate.getRecord(entityType);
    }
}
