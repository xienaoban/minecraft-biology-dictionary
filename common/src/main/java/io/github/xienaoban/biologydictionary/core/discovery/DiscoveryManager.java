package io.github.xienaoban.biologydictionary.core.discovery;

import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.config.ConfigsUpdateCallback;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.AlwaysUnlockedStrategy;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.DictionaryStrategy;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.KillBasedStrategy;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

/**
 * Server-side entry point for the discovery system.
 * Attached to {@link io.github.xienaoban.biologydictionary.core.session.WorldSession}.
 */
public final class DiscoveryManager implements ConfigsUpdateCallback {
    private volatile Configs.ServerConfigs.DiscoveryStrategyMode mode;
    private volatile DiscoveryStrategy strategy;

    public DiscoveryManager() {
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
            case ALWAYS_UNLOCKED -> new AlwaysUnlockedStrategy();
            case VANILLA_KILL -> new KillBasedStrategy();
            case DICTIONARY -> new DictionaryStrategy();
        };
    }

    /**
     * Check if an entity type is discovered by the player.
     */
    public boolean isDiscovered(Identifier entityType, ServerPlayer player) {
        if (player.isCreative()) {
            return true;
        }
        return strategy.isDiscovered(entityType, player);
    }

    public boolean isDiscovered(EntityType<?> entityType, ServerPlayer player) {
        return isDiscovered(Identifier.tryParse(EntityUtils.getEntityTypeIdName(entityType)), player);
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
