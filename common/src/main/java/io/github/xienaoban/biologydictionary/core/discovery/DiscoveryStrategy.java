package io.github.xienaoban.biologydictionary.core.discovery;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-side strategy for determining if an entity is discovered by a player.
 * Each strategy decides which event callbacks to respond to.
 */
public interface DiscoveryStrategy {
    /**
     * Check if the given entity type has been discovered by the player.
     * Creative mode check should be done by the caller (DiscoveryManager).
     */
    boolean isDiscovered(Identifier entityType, ServerPlayer player);

    /**
     * Called when the player kills an entity.
     */
    default void onEntityKilled(ServerPlayer player, Identifier entityType) {}

    /**
     * Called when the player tames an entity (future).
     */
    default void onEntityTamed(ServerPlayer player, Identifier entityType) {}

    /**
     * Called when the player uses the highlight skill on an entity type.
     */
    default void onEntityHighlighted(ServerPlayer player, Identifier entityType) {}

    /**
     * Called when the player views an entity in the dictionary overview screen.
     * @return the discovery record for incremental network sync, or null if no sync needed.
     */
    default DiscoveryRecord onEntityViewedInDictionary(ServerPlayer player, Identifier entityType) { return null; }

    /**
     * Called when the player uses the highlight skill on an entity type.
     * @return the discovery record for incremental network sync, or null if no sync needed.
     */
    default DiscoveryRecord onEntityHighlightedWithResult(ServerPlayer player, Identifier entityType) { return null; }

    /**
     * Save pending discovery data. Called on world save.
     */
    default void save() {}
}
