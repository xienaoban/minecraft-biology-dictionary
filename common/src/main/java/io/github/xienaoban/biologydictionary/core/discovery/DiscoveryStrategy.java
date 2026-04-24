package io.github.xienaoban.biologydictionary.core.discovery;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

/**
 * Server-side strategy for determining if an entity is discovered by a player.
 * Each strategy decides which event callbacks to respond to.
 */
public interface DiscoveryStrategy extends DiscoveryEventListener<ServerPlayer> {
    /**
     * Check if the given entity type has been discovered by the player.
     * Creative mode check should be done by the caller (DiscoveryManager).
     */
    boolean isDiscovered(ServerPlayer player, EntityType<?> entityType);
}
