package io.github.xienaoban.biologydictionary.api;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryManager;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

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
     * Discovery counts of the player across all trackable entity types.
     * Returns {@code (0, 0)} if the discovery manager is unavailable.
     */
    public static DiscoveryProgress getProgress(ServerPlayer player) {
        ServerWorldSession sws = ServerWorldSession.get();
        DiscoveryManager dm = sws != null ? sws.getDiscoveryManager() : null;
        WorldSession ws = WorldSession.get();
        if (dm == null || ws == null) {
            return new DiscoveryProgress(0, 0);
        }
        var entries = ws.getEntityManager().getEntityEntries();
        int discovered = 0;
        for (var entry : entries) {
            if (dm.isDiscovered(player, entry.getType())) { discovered++; }
        }
        return new DiscoveryProgress(discovered, entries.size());
    }

    /**
     * Record a discovery from a third-party {@link DiscoverySource}.
     * The source's {@link DiscoverySource#serverCheck} gate applies.
     * Returns {@code false} if the discovery manager is unavailable.
     * @return true if this event resulted in a new discovery
     */
    public static boolean recordDiscovery(DiscoverySource source, ServerPlayer player, Entity entity) {
        DiscoveryManager dm = manager();
        return dm != null && dm.onDiscoveryEvent(source, player, entity);
    }

    private static DiscoveryManager manager() {
        ServerWorldSession sws = ServerWorldSession.get();
        return sws != null ? sws.getDiscoveryManager() : null;
    }
}
