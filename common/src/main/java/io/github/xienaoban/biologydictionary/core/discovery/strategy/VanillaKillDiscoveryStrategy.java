package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryStrategy;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public final class VanillaKillDiscoveryStrategy implements DiscoveryStrategy {
	@Override
	public boolean isDiscovered(ServerPlayer player, EntityType<?> entityType) {
		boolean discovered = false;
		if (ConfigsManager.getServer().isDiscoveryByKill()) {
			discovered |= player.getStats().getValue(Stats.ENTITY_KILLED, entityType) > 0;
		}
		if (ConfigsManager.getServer().isDiscoveryByKilledBy()) {
			discovered |= player.getStats().getValue(Stats.ENTITY_KILLED_BY, entityType) > 0;
		}
		return discovered;
	}

	@Override
	public boolean onEntityKilled(ServerPlayer player, Entity entity) {
		EntityType<?> entityType = EntityUtils.getEntityType(entity);
		if (player.getStats().getValue(Stats.ENTITY_KILLED, entityType) == 0) {
			DiscoveryRecord record = DiscoveryRecord.discoveredNow(player.level().getGameTime(), entity, DiscoverySource.KILL);
			ServerNetManager.sendDiscoveryIncremental(player, entity, entityType, record);
			return true;
		}
		return false;
	}

	@Override
	public boolean onPlayerKilledBy(ServerPlayer player, Entity entity) {
		EntityType<?> entityType = EntityUtils.getEntityType(entity);
		if (player.getStats().getValue(Stats.ENTITY_KILLED_BY, entityType) == 0) {
			DiscoveryRecord record = DiscoveryRecord.discoveredNow(player.level().getGameTime(), entity, DiscoverySource.KILLED_BY);
			ServerNetManager.sendDiscoveryIncremental(player, entity, entityType, record);
			return true;
		}
		return false;
	}
}
