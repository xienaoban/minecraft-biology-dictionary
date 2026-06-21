package io.github.xienaoban.biologydictionary.core.discovery;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Listener interface for discovery events.
 * Both {@link ClientDiscoveryCache} and {@link DiscoveryStrategy} implement this
 * so that discovery logic can live on either side (client or server).
 *
 * @param <P> the player type (e.g. {@link net.minecraft.server.level.ServerPlayer} on server,
 *            {@link net.minecraft.client.player.LocalPlayer} on client)
 */
public interface DiscoveryEventListener<P extends Player> {

    /**
     * Called when the player opens the entity detail screen.
     * @return true if this event resulted in a new discovery
     */
    default boolean onEntityDetailScreenOpened(P player, Entity entity) { return false; }

    /**
     * Called when the player uses the highlight skill on an entity.
     * @return true if this event resulted in a new discovery
     */
    default boolean onEntityHighlighted(P player, Entity entity) { return false; }

    /**
     * Called when the player uses the telescope/spyglass to observe an entity.
     * @return true if this event resulted in a new discovery
     */
    default boolean onEntityObservedWithTelescope(P player, Entity entity) { return false; }

    /**
     * Called when the player right-clicks / interacts with an entity.
     * @return true if this event resulted in a new discovery
     */
    default boolean onEntityInteracted(P player, Entity entity) { return false; }

    /**
     * Called when the player kills an entity.
     * @return true if this event resulted in a new discovery
     */
    default boolean onEntityKilled(P player, Entity entity) { return false; }

    /**
     * Called when the player is killed by an entity.
     * @return true if this event resulted in a new discovery
     */
    default boolean onPlayerKilledBy(P player, Entity entity) { return false; }
}
