package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.api.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.api.DiscoverySource;
import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySources;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.EntityType;

/**
 * Queries MC Stats locally to determine discovery.
 * No network sync needed — MC syncs kill stats to the client automatically.
 */
@ClientOnly
public final class VanillaKillClientDiscoveryCache implements ClientDiscoveryCache {
    private final DiscoveryRecord kill = DiscoveryRecord.simple(DiscoverySources.KILL);
    private final DiscoveryRecord killedBy = DiscoveryRecord.simple(DiscoverySources.KILLED_BY);

    @Override
    public boolean isDiscovered(EntityType<?> entityType) {
        return getRecord(entityType) != null;
    }

    @Override
    public DiscoveryRecord getRecord(EntityType<?> entityType) {
        LocalPlayer player = ClientUtils.getClientPlayer();
        if (ClientUtils.getClientPlayer() == null) {
            return null;
        }

        StatsCounter stats = player.getStats();
        Configs.ServerConfigs serverConfig = ConfigsManager.getServer();
        if (serverConfig.isDiscoveryByKill()) {
            if (stats.getValue(Stats.ENTITY_KILLED, entityType) > 0) {
                return kill;
            }
        }
        if (serverConfig.isDiscoveryByKilledBy()) {
            if (stats.getValue(Stats.ENTITY_KILLED_BY, entityType) > 0) {
                return killedBy;
            }
        }
        return null;
    }

    @Override
    public void incrementalSync(EntityType<?> entityType, DiscoveryRecord record) {}

    @Override
    public boolean onDiscovery(DiscoverySource source, DiscoverySource.ClientContext ctx) {
        return false;
    }
}
