package io.github.xienaoban.biologydictionary.core.discovery;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;

/**
 * Client-side cache for discovery data.
 * Each DiscoveryStrategyMode has a corresponding implementation.
 */
@Environment(EnvType.CLIENT)
public interface ClientDiscoveryCache extends DiscoveryEventListener<LocalPlayer> {
    boolean isDiscovered(EntityType<?> entityType);

    DiscoveryRecord getRecord(EntityType<?> entityType);

    default void incrementalSync(EntityType<?> entityType, DiscoveryRecord record) {}
}
