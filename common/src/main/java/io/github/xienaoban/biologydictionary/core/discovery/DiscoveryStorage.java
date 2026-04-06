package io.github.xienaoban.biologydictionary.core.discovery;

import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.UUID;

/**
 * Backend-agnostic storage interface for discovery data.
 * Allows swapping between SavedData, YAML, JSON, etc.
 */
public interface DiscoveryStorage {
    DiscoveryRecord get(UUID playerUUID, Identifier entityType);

    Map<Identifier, DiscoveryRecord> getAll(UUID playerUUID);

    void put(UUID playerUUID, Identifier entityType, DiscoveryRecord record);

    void setDirty();
}
