package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

/**
 * Queries MC Stats locally to determine discovery.
 * No network sync needed — MC syncs kill stats to the client automatically.
 */
@Environment(EnvType.CLIENT)
public final class VanillaKillClientDiscoveryCache implements ClientDiscoveryCache {

    @Override
    public boolean isDiscovered(Identifier entityType) {
        if (ClientUtils.getClientPlayer() == null) {
            return false;
        }
        EntityType<?> type = EntityUtils.getEntityType(entityType);
        if (type == null) {
            return false;
        }
        return ClientUtils.getClientPlayer().getStats()
                .getValue(Stats.ENTITY_KILLED, type) > 0;
    }

    @Override
    public DiscoveryRecord getRecord(Identifier entityType) {
        return isDiscovered(entityType)
                ? new DiscoveryRecord(true)
                : DiscoveryRecord.UNDISCOVERED;
    }

    @Override
    public void onFullSync(Map<Identifier, DiscoveryRecord> data) {}

    @Override
    public void onIncrementalSync(Identifier entityType, DiscoveryRecord record) {}
}
