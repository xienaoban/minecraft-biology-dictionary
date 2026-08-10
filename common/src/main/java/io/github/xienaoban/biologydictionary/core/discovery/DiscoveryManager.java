package io.github.xienaoban.biologydictionary.core.discovery;

import io.github.xienaoban.biologydictionary.api.DiscoverySource;
import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.config.ConfigsUpdateCallback;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.AlwaysUnlockedDiscoveryStrategy;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.BiologyDictionaryDiscoveryStrategy;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.VanillaKillDiscoveryStrategy;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * Server-side entry point for the discovery system.
 * Attached to {@link io.github.xienaoban.biologydictionary.core.session.ServerWorldSession}.
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

    public DiscoveryStrategy getStrategy() {
        return strategy;
    }

    public Configs.ServerConfigs.DiscoveryStrategyMode getMode() {
        return mode;
    }

    public boolean isDiscovered(ServerPlayer player, EntityType<?> entityType) {
        return strategy.isDiscovered(player, entityType);
    }

    /**
     * Get the discovery record for the entity type, or {@code null} if undiscovered.
     * Same pure-query semantics as {@link #isDiscovered}.
     */
    public DiscoveryRecord getRecord(ServerPlayer player, EntityType<?> entityType) {
        return strategy.getRecord(player, entityType);
    }

    public boolean onDiscoveryEvent(DiscoverySource source, ServerPlayer player, Entity entity) {
        if (!source.isEnabled()) { return false; }
        if (EntityManager.isEntityTypeBlacklisted(entity.getType())) { return false; }
        return strategy.onDiscovery(source, new DiscoverySource.ServerContext(player, entity));
    }
}
