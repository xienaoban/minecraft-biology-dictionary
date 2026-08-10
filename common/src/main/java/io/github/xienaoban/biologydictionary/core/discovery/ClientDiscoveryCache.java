package io.github.xienaoban.biologydictionary.core.discovery;

import io.github.xienaoban.biologydictionary.api.DiscoverySource;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.world.entity.EntityType;

/**
 * Client-side cache for discovery data.
 * Each DiscoveryStrategyMode has a corresponding implementation.
 */
@ClientOnly
public interface ClientDiscoveryCache {

    boolean isDiscovered(EntityType<?> entityType);

    DiscoveryRecord getRecord(EntityType<?> entityType);

    void incrementalSync(EntityType<?> entityType, DiscoveryRecord record);

    /**
     * Handle a discovery event on the client.
     * @return true if this event resulted in a (client-optimistic) request
     */
    boolean onDiscovery(DiscoverySource source, DiscoverySource.ClientContext ctx);
}
