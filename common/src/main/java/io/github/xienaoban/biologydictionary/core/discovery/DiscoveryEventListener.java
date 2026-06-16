package io.github.xienaoban.biologydictionary.core.discovery;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Listener interface for discovery events.
 */
public interface DiscoveryEventListener<P extends Player> {
	default boolean onEntityDetailScreenOpened(P player, Entity entity) { return false; }

	default boolean onEntityHighlighted(P player, Entity entity) { return false; }

	default boolean onEntityObservedWithTelescope(P player, Entity entity) { return false; }

	default boolean onEntityInteracted(P player, Entity entity) { return false; }

	default boolean onEntityKilled(P player, Entity entity) { return false; }

	default boolean onPlayerKilledBy(P player, Entity entity) { return false; }
}
