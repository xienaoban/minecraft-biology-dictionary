package io.github.xienaoban.biologydictionary.core.discovery;

import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.UUID;

/**
 * Backend-agnostic storage interface for discovery data.
 * Allows swapping between SavedData, YAML, JSON, etc.
 */
public interface DiscoveryStorage {
    DiscoveryRecord get(UUID playerUUID, EntityType<?> entityType);

    Map<EntityType<?>, DiscoveryRecord> getAll(UUID playerUUID);

    void put(UUID playerUUID, EntityType<?> entityType, DiscoveryRecord record);

    void setDirty();
}
