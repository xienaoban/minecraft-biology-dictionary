package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;

/**
 * Queries MC Stats locally to determine discovery.
 * No network sync needed — MC syncs kill stats to the client automatically.
 */
@Environment(EnvType.CLIENT)
public final class VanillaKillClientDiscoveryCache implements ClientDiscoveryCache {

    @Override
    public boolean isDiscovered(EntityType<?> entityType) {
        if (ClientUtils.getClientPlayer() == null) {
            return false;
        }
        return ClientUtils.getClientPlayer().getStats()
                .getValue(Stats.ENTITY_KILLED, entityType) > 0;
    }

    @Override
    public DiscoveryRecord getRecord(EntityType<?> entityType) {
        return isDiscovered(entityType)
                ? new DiscoveryRecord(true)
                : DiscoveryRecord.UNDISCOVERED;
    }
}
