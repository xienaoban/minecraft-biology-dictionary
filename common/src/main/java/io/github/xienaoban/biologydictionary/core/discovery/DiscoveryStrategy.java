package io.github.xienaoban.biologydictionary.core.discovery;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

/**
 * Server-side strategy for determining if an entity is discovered by a player.
 * Each strategy decides which discovery sources to respond to via {@link #onDiscovery}.
 */
public interface DiscoveryStrategy {

    /**
     * Check if the given entity type has been discovered by the player.
     * Creative mode check should be done by the caller (DiscoveryManager).
     */
    boolean isDiscovered(ServerPlayer player, EntityType<?> entityType);

    /**
     * Handle a discovery event. Each strategy decides which sources to record;
     * strategies that don't record anything simply return false.
     * @return true if this event resulted in a new discovery
     */
    boolean onDiscovery(DiscoverySource source, DiscoverySource.ServerContext ctx);
}
