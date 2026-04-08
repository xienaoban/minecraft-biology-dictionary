package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryStrategy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

/**
 * All entities are always discovered. No events trigger any action.
 */
public final class AlwaysUnlockedDiscoveryStrategy implements DiscoveryStrategy {

    @Override
    public boolean isDiscovered(ServerPlayer player, EntityType<?> entityType) {
        return true;
    }
}
