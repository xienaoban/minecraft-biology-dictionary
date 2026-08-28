package io.github.xienaoban.biologydictionary.core.discovery;

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
 * The strategy mode and the global-shared flag together select the delegate; any
 * change rebuilds it. Rebuilding the {@link BiologyDictionaryClientDiscoveryCache}
 * re-pulls the full effective view, which is how a global-shared toggle is applied.
 */
@ClientOnly
public final class ClientDiscoveryCacheManager implements ConfigsUpdateCallback {
    private volatile Configs.ServerConfigs.DiscoveryStrategyMode mode;
    private volatile boolean globalShared;
    private volatile ClientDiscoveryCache delegate;

    public ClientDiscoveryCacheManager() {
        onConfigsUpdate(ConfigsManager.getClient(), ConfigsManager.getServer());
    }

    @Override
    public void onConfigsUpdate(Configs.ClientConfigs clientConfigs, Configs.ServerConfigs serverConfigs) {
        Configs.ServerConfigs.DiscoveryStrategyMode newMode = serverConfigs.getDiscoveryStrategy();
        boolean newShared = serverConfigs.isDiscoveryGlobalShared();
        if (newMode == mode && newShared == globalShared) {
            return;
        }
        mode = newMode;
        globalShared = newShared;
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
