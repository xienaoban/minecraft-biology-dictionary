package io.github.xienaoban.biologydictionary.core.discovery;

import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.config.ConfigsUpdateCallback;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.AlwaysUnlockedClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.BiologyDictionaryClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.VanillaKillClientDiscoveryCache;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
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
        return delegate.isDiscovered(entityType);
    }

    @Override
    public DiscoveryRecord getRecord(EntityType<?> entityType) {
        return delegate.getRecord(entityType);
    }

    @Override
    public void incrementalSync(EntityType<?> entityType, DiscoveryRecord record) {
        delegate.incrementalSync(entityType, record);
    }

    @Override
    public boolean onEntityDetailScreenOpened(LocalPlayer player, Entity entity) {
        if (!ConfigsManager.getServer().isDiscoveryByDetailScreen()) { return false; }
        return delegate.onEntityDetailScreenOpened(player, entity);
    }

    @Override
    public boolean onEntityHighlighted(LocalPlayer player, Entity entity) {
        if (!ConfigsManager.getServer().isDiscoveryByHighlight()) { return false; }
        return delegate.onEntityHighlighted(player, entity);
    }

    @Override
    public boolean onEntityObservedWithTelescope(LocalPlayer player, Entity entity) {
        if (!ConfigsManager.getServer().isDiscoveryByTelescope()) { return false; }
        return delegate.onEntityObservedWithTelescope(player, entity);
    }

    @Override
    public boolean onEntityInteracted(LocalPlayer player, Entity entity) {
        if (!ConfigsManager.getServer().isDiscoveryByInteract()) { return false; }
        return delegate.onEntityInteracted(player, entity);
    }

    @Override
    public boolean onEntityKilled(LocalPlayer player, Entity entity) {
        if (!ConfigsManager.getServer().isDiscoveryByKill()) { return false; }
        return delegate.onEntityKilled(player, entity);
    }

    @Override
    public boolean onPlayerKilledBy(LocalPlayer player, Entity entity) {
        if (!ConfigsManager.getServer().isDiscoveryByKilledBy()) { return false; }
        return delegate.onPlayerKilledBy(player, entity);
    }
}
