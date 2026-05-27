package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.EntityType;

/**
 * Queries MC Stats locally to determine discovery.
 * No network sync needed — MC syncs kill stats to the client automatically.
 */
public final class VanillaKillClientDiscoveryCache implements ClientDiscoveryCache {
    private final DiscoveryRecord kill = new DiscoveryRecord(DiscoverySource.KILL);
    private final DiscoveryRecord killedBy = new DiscoveryRecord(DiscoverySource.KILLED_BY);

    @Override
    public boolean isDiscovered(EntityType<?> entityType) {
        return getRecord(entityType) != null;
    }

    @Override
    public DiscoveryRecord getRecord(EntityType<?> entityType) {
        if (ClientUtils.getClientPlayer() == null) {
            return null;
        }

        StatsCounter stats = ClientUtils.getClientPlayer().getStats();
        if (ConfigsManager.getServer().isDiscoveryByKill()) {
            if (stats.getValue(Stats.ENTITY_KILLED, entityType) > 0) {
                return kill;
            }
        }
        if (ConfigsManager.getServer().isDiscoveryByKilledBy()) {
            if (stats.getValue(Stats.ENTITY_KILLED_BY, entityType) > 0) {
                return killedBy;
            }
        }
        return null;
    }
}
