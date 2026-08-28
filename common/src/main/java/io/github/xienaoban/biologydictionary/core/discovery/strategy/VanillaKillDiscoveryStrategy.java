package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySources;
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
    public DiscoveryRecord getRecord(ServerPlayer player, EntityType<?> entityType) {
        if (ConfigsManager.getServer().isDiscoveryByKill()
                && player.getStats().getValue(Stats.ENTITY_KILLED, entityType) > 0) {
            return DiscoveryRecord.simple(DiscoverySources.KILL);
        }
        if (ConfigsManager.getServer().isDiscoveryByKilledBy()
                && player.getStats().getValue(Stats.ENTITY_KILLED_BY, entityType) > 0) {
            return DiscoveryRecord.simple(DiscoverySources.KILLED_BY);
        }
        return null;
    }

    @Override
    public boolean onDiscovery(DiscoverySource source, DiscoverySource.ServerContext ctx) {
        ServerPlayer player = ctx.player();
        Entity entity = ctx.entity();
        EntityType<?> entityType = EntityUtils.getEntityType(entity);
        long gameTick = player.level().getGameTime();
        // Injected before the stat is awarded, so the stat value is still the pre-event one.
        if (source == DiscoverySources.KILL) {
            if (player.getStats().getValue(Stats.ENTITY_KILLED, entityType) == 0) {
                send(player, entity, entityType, DiscoveryRecord.standard(gameTick, entity, source, player));
                return true;
            }
        } else if (source == DiscoverySources.KILLED_BY) {
            if (player.getStats().getValue(Stats.ENTITY_KILLED_BY, entityType) == 0) {
                send(player, entity, entityType, DiscoveryRecord.standard(gameTick, entity, source, player));
                return true;
            }
        }
        return false;
    }

    private static void send(ServerPlayer player, Entity entity, EntityType<?> entityType, DiscoveryRecord record) {
        ServerNetManager.sendDiscoveryIncremental(player, entity, entityType, record);
    }
}
