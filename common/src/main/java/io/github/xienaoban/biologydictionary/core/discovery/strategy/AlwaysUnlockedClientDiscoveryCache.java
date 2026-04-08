package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

import java.util.Map;

/**
 * All entities are always discovered. No network sync needed.
 */
@Environment(EnvType.CLIENT)
public final class AlwaysUnlockedClientDiscoveryCache implements ClientDiscoveryCache {

    @Override
    public boolean isDiscovered(Identifier entityType) {
        return true;
    }

    @Override
    public DiscoveryRecord getRecord(Identifier entityType) {
        return new DiscoveryRecord(true);
    }

    @Override
    public void onFullSync(Map<Identifier, DiscoveryRecord> data) {}

    @Override
    public void onIncrementalSync(Identifier entityType, DiscoveryRecord record) {}
}
