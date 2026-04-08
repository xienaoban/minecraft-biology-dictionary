package io.github.xienaoban.biologydictionary.core.discovery;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

import java.util.Map;

/**
 * Client-side cache for discovery data.
 * Each DiscoveryStrategyMode has a corresponding implementation.
 */
@Environment(EnvType.CLIENT)
public interface ClientDiscoveryCache {
    boolean isDiscovered(Identifier entityType);

    DiscoveryRecord getRecord(Identifier entityType);

    /**
     * Called on player login or full data sync.
     */
    void onFullSync(Map<Identifier, DiscoveryRecord> data);

    /**
     * Called when a single entity is newly discovered.
     */
    void onIncrementalSync(Identifier entityType, DiscoveryRecord record);
}
