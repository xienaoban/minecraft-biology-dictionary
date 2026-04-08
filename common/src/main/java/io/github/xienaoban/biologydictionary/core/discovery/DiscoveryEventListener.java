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
     */
    default void onEntityDetailScreenOpened(P player, Entity entity) {}

    /**
     * Called when the player uses the highlight skill on an entity.
     */
    default void onEntityHighlighted(P player, Entity entity) {}

    /**
     * Called when the player uses the telescope/spyglass to observe an entity.
     */
    default void onEntityObservedWithTelescope(P player, Entity entity) {}

    /**
     * Called when the player kills an entity.
     */
    default void onEntityKilled(P player, Entity entity) {}

    /**
     * Called when the player attacks an entity.
     */
    default void onEntityAttacked(P player, Entity entity) {}

    /**
     * Called when the player right-clicks / interacts with an entity.
     */
    default void onEntityInteracted(P player, Entity entity) {}

    /**
     * Called when the player feeds an entity.
     */
    default void onEntityFed(P player, Entity entity) {}

    /**
     * Called when the player tames an entity.
     */
    default void onEntityTamed(P player, Entity entity) {}
}
