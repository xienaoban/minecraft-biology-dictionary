package io.github.xienaoban.biologydictionary.core.discovery;

import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
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
public final class DiscoveryManager {
    private Configs.ServerConfigs.DiscoveryStrategyMode mode;
    private DiscoveryStrategy strategy;

    public DiscoveryManager() {
        this.mode = Configs.ServerConfigs.DiscoveryStrategyMode.ALWAYS_UNLOCKED;
        this.strategy = AlwaysUnlockedStrategy.INSTANCE;
    }

    /**
     * Reload strategy from current config.
     */
    public void onConfigsUpdate() {
        Configs.ServerConfigs.DiscoveryStrategyMode newMode = ConfigsManager.getServer().getDiscoveryStrategy();
        if (newMode == mode) {
            return;
        }
        mode = newMode;
        strategy = switch (newMode) {
            case ALWAYS_UNLOCKED -> AlwaysUnlockedStrategy.INSTANCE;
            case VANILLA_KILL -> KillBasedStrategy.INSTANCE;
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
     * Get all discovery records for the player. Returns null if not applicable (e.g. non-DICTIONARY strategy).
     */
    public Map<Identifier, DiscoveryRecord> getDiscoveryRecords(ServerPlayer player) {
        return strategy.getAllRecords(player);
    }

    /**
     * Save pending discovery data. Called on world save.
     */
    public void save() {
        strategy.save();
    }
}
