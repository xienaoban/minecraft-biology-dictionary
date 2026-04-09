package io.github.xienaoban.biologydictionary.core.discovery;

import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.config.ConfigsUpdateCallback;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.AlwaysUnlockedClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.BiologyDictionaryClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.VanillaKillClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * A {@link ClientDiscoveryCache} that delegates to another cache instance.
 * Allows the delegate to be swapped based on the current server config.
 */
@Environment(EnvType.CLIENT)
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

    @Override
    public boolean onEntityDetailScreenOpened(LocalPlayer player, Entity entity) {
        return delegate.onEntityDetailScreenOpened(player, entity);
    }

    @Override
    public boolean onEntityHighlighted(LocalPlayer player, Entity entity) {
        return delegate.onEntityHighlighted(player, entity);
    }

    @Override
    public boolean onEntityObservedWithTelescope(LocalPlayer player, Entity entity) {
        return delegate.onEntityObservedWithTelescope(player, entity);
    }

    @Override
    public boolean onEntityKilled(LocalPlayer player, Entity entity) {
        return delegate.onEntityKilled(player, entity);
    }

    @Override
    public boolean onEntityAttacked(LocalPlayer player, Entity entity) {
        return delegate.onEntityAttacked(player, entity);
    }

    @Override
    public boolean onEntityInteracted(LocalPlayer player, Entity entity) {
        return delegate.onEntityInteracted(player, entity);
    }

    @Override
    public boolean onEntityFed(LocalPlayer player, Entity entity) {
        return delegate.onEntityFed(player, entity);
    }

    @Override
    public boolean onEntityTamed(LocalPlayer player, Entity entity) {
        return delegate.onEntityTamed(player, entity);
    }
}
