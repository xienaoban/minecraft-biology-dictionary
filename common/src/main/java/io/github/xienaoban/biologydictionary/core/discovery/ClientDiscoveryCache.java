package io.github.xienaoban.biologydictionary.core.discovery;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;

/**
 * Client-side cache for discovery data.
 * Each DiscoveryStrategyMode has a corresponding implementation.
 */
public interface ClientDiscoveryCache extends DiscoveryEventListener<LocalPlayer> {
    boolean isDiscovered(EntityType<?> entityType);

    DiscoveryRecord getRecord(EntityType<?> entityType);

    default void incrementalSync(EntityType<?> entityType, DiscoveryRecord record) {}
}
