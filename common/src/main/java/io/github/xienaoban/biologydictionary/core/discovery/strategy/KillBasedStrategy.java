package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryStrategy;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;

/**
 * Entity is discovered when the player has killed it at least once.
 * Uses MC's native Stats.ENTITY_KILLED — no extra storage needed.
 */
public final class KillBasedStrategy implements DiscoveryStrategy {

    @Override
    public boolean isDiscovered(Identifier entityType, ServerPlayer player) {
        EntityType<?> type = EntityUtils.getEntityType(entityType);
        if (type == null) {
            return false;
        }
        return player.getStats().getValue(Stats.ENTITY_KILLED, type) > 0;
    }
}
