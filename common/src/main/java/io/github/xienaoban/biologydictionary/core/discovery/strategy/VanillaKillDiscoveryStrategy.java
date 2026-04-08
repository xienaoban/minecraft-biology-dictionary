package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryStrategy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;

/**
 * Entity is discovered when the player has killed it at least once.
 * Uses MC's native Stats.ENTITY_KILLED — no extra storage needed.
 */
public final class VanillaKillDiscoveryStrategy implements DiscoveryStrategy {

    @Override
    public boolean isDiscovered(ServerPlayer player, EntityType<?> entityType) {
        return player.getStats().getValue(Stats.ENTITY_KILLED, entityType) > 0;
    }
}
