package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.world.entity.EntityType;

/**
 * All entities are always discovered. No network sync needed.
 */
@ClientOnly
public final class AlwaysUnlockedClientDiscoveryCache implements ClientDiscoveryCache {

    @Override
    public boolean isDiscovered(EntityType<?> entityType) {
        return true;
    }

    @Override
    public DiscoveryRecord getRecord(EntityType<?> entityType) {
        return new DiscoveryRecord();
    }
}
