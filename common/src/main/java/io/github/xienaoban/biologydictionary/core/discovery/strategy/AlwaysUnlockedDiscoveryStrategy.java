package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryStrategy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

public final class AlwaysUnlockedDiscoveryStrategy implements DiscoveryStrategy {
	@Override
	public boolean isDiscovered(ServerPlayer player, EntityType<?> entityType) {
		return true;
	}
}
