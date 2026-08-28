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
 * The strategy mode selects the delegate type (rebuilt on change); the global-shared
 * flag only changes the data set, so instead of a rebuild it triggers a resync of the
 * {@link BiologyDictionaryClientDiscoveryCache}: turning it off is a pure local drop
 * of the global view entries, turning it on re-pulls the full view.
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
        boolean modeChanged = newMode != mode;
        mode = newMode;
        globalShared = newShared;
        if (modeChanged) {
            delegate = switch (newMode) {
                case ALWAYS_UNLOCKED -> new AlwaysUnlockedClientDiscoveryCache();
                case VANILLA_KILL -> new VanillaKillClientDiscoveryCache();
                case BIOLOGY_DICTIONARY -> new BiologyDictionaryClientDiscoveryCache();
            };
        }
        if (delegate instanceof BiologyDictionaryClientDiscoveryCache cache) {
            if (globalShared || modeChanged) {
                cache.requestFullSync();
            } else {
                cache.dropGlobalRecords();
            }
        }
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
