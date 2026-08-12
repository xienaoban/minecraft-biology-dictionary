package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.api.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.api.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySources;
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

    @Override
    public DiscoveryRecord getRecord(ServerPlayer player, EntityType<?> entityType) {
        return DiscoveryRecord.simple(DiscoverySources.UNKNOWN);
    }

    @Override
    public boolean onDiscovery(DiscoverySource source, DiscoverySource.ServerContext ctx) {
        return false;
    }
}
