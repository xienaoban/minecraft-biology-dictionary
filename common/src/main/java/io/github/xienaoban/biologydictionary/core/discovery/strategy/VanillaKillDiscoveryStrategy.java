package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryStrategy;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * Entity is discovered when the player has killed it at least once.
 * Uses MC's native Stats.ENTITY_KILLED / ENTITY_KILLED_BY — no extra storage needed.
 */
public final class VanillaKillDiscoveryStrategy implements DiscoveryStrategy {

    @Override
    public boolean isDiscovered(ServerPlayer player, EntityType<?> entityType) {
        return player.getStats().getValue(Stats.ENTITY_KILLED, entityType) > 0
                || player.getStats().getValue(Stats.ENTITY_KILLED_BY, entityType) > 0;
    }

    @Override
    public boolean onEntityKilled(ServerPlayer player, Entity entity) {
        EntityType<?> entityType = EntityUtils.getEntityType(entity);
        // Injected at HEAD of Player.killedEntity, stats not yet updated
        if (player.getStats().getValue(Stats.ENTITY_KILLED, entityType) == 0) {
            DiscoveryRecord record = DiscoveryRecord.discoveredNow(
                    player.level().getGameTime(), entity, DiscoverySource.KILL);
            ServerNetManager.sendDiscoveryIncremental(player, entityType, record);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPlayerKilledByEntity(ServerPlayer player, Entity entity) {
        EntityType<?> entityType = EntityUtils.getEntityType(entity);
        // Injected before awardStat in ServerPlayer.die, stats not yet updated
        if (player.getStats().getValue(Stats.ENTITY_KILLED_BY, entityType) == 0) {
            DiscoveryRecord record = DiscoveryRecord.discoveredNow(
                    player.level().getGameTime(), entity, DiscoverySource.KILLED_BY);
            ServerNetManager.sendDiscoveryIncremental(player, entityType, record);
            return true;
        }
        return false;
    }
}
