package io.github.xienaoban.biologydictionary.core.discovery;

import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.config.ConfigsUpdateCallback;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.AlwaysUnlockedDiscoveryStrategy;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.BiologyDictionaryDiscoveryStrategy;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.VanillaKillDiscoveryStrategy;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

/**
 * Server-side entry point for the discovery system.
 * Attached to {@link io.github.xienaoban.biologydictionary.core.session.WorldSession}.
 */
public final class DiscoveryManager implements ConfigsUpdateCallback {
    private final MinecraftServer server;

    private volatile Configs.ServerConfigs.DiscoveryStrategyMode mode;
    private volatile DiscoveryStrategy strategy;

    public DiscoveryManager(MinecraftServer server) {
        this.server = server;
        onConfigsUpdate(ConfigsManager.getClient(), ConfigsManager.getServer());
    }

    @Override
    public void onConfigsUpdate(Configs.ClientConfigs clientConfigs, Configs.ServerConfigs serverConfigs) {
        Configs.ServerConfigs.DiscoveryStrategyMode newMode = serverConfigs.getDiscoveryStrategy();
        if (newMode == mode) {
            return;
        }
        mode = newMode;
        strategy = switch (newMode) {
            case ALWAYS_UNLOCKED -> new AlwaysUnlockedDiscoveryStrategy();
            case VANILLA_KILL -> new VanillaKillDiscoveryStrategy();
            case BIOLOGY_DICTIONARY -> new BiologyDictionaryDiscoveryStrategy(server);
        };
    }

    /**
     * Check if an entity type is discovered by the player.
     */
    public boolean isDiscovered(ServerPlayer player, EntityType<?> entityType) {
        if (player.isCreative()) {
            return true;
        }
        return strategy.isDiscovered(player, entityType);
    }

    public DiscoveryStrategy getStrategy() {
        return strategy;
    }

    public Configs.ServerConfigs.DiscoveryStrategyMode getMode() {
        return mode;
    }

    /**
     * Save pending discovery data. Called on world save.
     */
    public void save() {
        strategy.save();
    }
}
