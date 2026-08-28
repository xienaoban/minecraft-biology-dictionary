package io.github.xienaoban.biologydictionary.core.discovery;

import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Resident in-memory statistics of genuine discoveries across all players, keyed by
 * entity type and ordered by game tick then wall-clock time (the first entry is
 * the earliest record). Derived once from the pools when the discovery saved data loads, then
 * maintained by the storage itself: every {@code put} whose record's discoverer is
 * the pool owner appends here. Independent of the global-shared toggle, so future
 * server-wide stats features can reuse it.
 *
 * <p>Only genuine self-discoveries participate: a record counts for its pool owner
 * iff its discoverer is the owner (legacy records get the owner filled in by the
 * load-time migration). Server-thread only.
 */
public final class GlobalDiscoveryStats {

    private static final Comparator<DiscoveryRecord> ORDER = Comparator
            .comparingLong(DiscoveryRecord::gameTick)
            .thenComparingLong(DiscoveryRecord::realTime);

    private final Map<EntityType<?>, List<DiscoveryRecord>> stats = new HashMap<>();

    /**
     * Derive the stats from all existing player pools; called once at load.
     */
    public void deriveFrom(Map<UUID, Map<EntityType<?>, DiscoveryRecord>> pools) {
        for (Map.Entry<UUID, Map<EntityType<?>, DiscoveryRecord>> pool : pools.entrySet()) {
            for (Map.Entry<EntityType<?>, DiscoveryRecord> entry : pool.getValue().entrySet()) {
                DiscoveryRecord record = entry.getValue();
                if (!record.discoverer().equals(pool.getKey())) {
                    continue;
                }
                append(entry.getKey(), record);
            }
        }
        for (List<DiscoveryRecord> entries : stats.values()) {
            entries.sort(ORDER);
        }
    }

    /**
     * The earliest record for the entity type, or {@code null} if none.
     */
    public DiscoveryRecord earliestRecord(EntityType<?> entityType) {
        List<DiscoveryRecord> entries = stats.get(entityType);
        return entries != null ? entries.get(0) : null;
    }

    public boolean contains(EntityType<?> entityType) {
        return stats.containsKey(entityType);
    }

    /**
     * Record a genuine discovery; called right after the record enters its owner's pool.
     */
    public void append(EntityType<?> entityType, DiscoveryRecord record) {
        stats.computeIfAbsent(entityType, key -> new ArrayList<>()).add(record);
    }

    /**
     * All entity types that have at least one genuine discovery.
     */
    public Set<EntityType<?>> types() {
        return stats.keySet();
    }
}
