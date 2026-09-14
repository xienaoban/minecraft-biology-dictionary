package io.github.xienaoban.biologydictionary.api;

import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryManager;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Optional;

/**
 * Server-side static facade for the discovery system.
 *
 * <p>All methods are pure queries and do <b>not</b> consider creative mode.
 * If you want {@code creative || discovered} semantics, check
 * {@link ServerPlayer#isCreative()} yourself.</p>
 */
public final class ServerDiscoveryApi {
    private ServerDiscoveryApi() {}

    /**
     * Whether the entity type has been discovered by the player.
     * Returns {@code false} if the discovery manager is unavailable.
     */
    public static boolean isDiscovered(ServerPlayer player, EntityType<?> type) {
        DiscoveryManager dm = manager();
        return dm != null && dm.isDiscovered(player, type);
    }

    /**
     * Get the discovery record for the entity type, or {@link Optional#empty()} if undiscovered.
     * Requires an online player. Returns {@link Optional#empty()} if the discovery manager
     * is unavailable.
     */
    public static Optional<DiscoveryRecord> getRecord(ServerPlayer player, EntityType<?> type) {
        DiscoveryManager dm = manager();
        return dm == null ? Optional.empty() : Optional.ofNullable(dm.getRecord(player, type));
    }

    /**
     * Discovered entity entries of the player, filtered from the given entries
     * (e.g. the result of {@link EntityInfoApi#getTotalEntities()}).
     * Returns an empty list if the discovery manager is unavailable.
     */
    public static List<EntityDictionaryEntry> getDiscoveredEntities(ServerPlayer player, List<EntityDictionaryEntry> entries) {
        ServerWorldSession sws = ServerWorldSession.get();
        DiscoveryManager dm = sws != null ? sws.getDiscoveryManager() : null;
        if (dm == null) { return List.of(); }
        return entries.stream()
                .filter(entry -> dm.isDiscovered(player, entry.getType()))
                .toList();
    }

    /**
     * Record a discovery from a third-party {@link DiscoverySource}.
     * The source's {@link DiscoverySource#serverCheck} gate applies.
     * <p>
     * Note the return semantics differ from {@link ClientDiscoveryApi#recordDiscovery}:
     * here {@code true} means a new discovery was actually recorded, not merely requested.
     * Returns {@code false} if the discovery manager is unavailable.
     *
     * @return true if this event resulted in a new discovery
     */
    public static boolean recordDiscovery(ServerPlayer player, DiscoverySource source, Entity entity) {
        DiscoveryManager dm = manager();
        return dm != null && dm.onDiscoveryEvent(source, player, entity);
    }

    private static DiscoveryManager manager() {
        ServerWorldSession sws = ServerWorldSession.get();
        return sws != null ? sws.getDiscoveryManager() : null;
    }
}
