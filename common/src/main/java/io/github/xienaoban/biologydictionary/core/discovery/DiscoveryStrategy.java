package io.github.xienaoban.biologydictionary.core.discovery;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

public interface DiscoveryStrategy extends DiscoveryEventListener<ServerPlayer> {
    boolean isDiscovered(ServerPlayer player, EntityType<?> entityType);
}
