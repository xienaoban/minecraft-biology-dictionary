package io.github.xienaoban.biologydictionary.api;

import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCacheManager;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

/**
 * Client-side static facade for the discovery system.
 * All queries act on the current local player's cache.
 *
 * <p>The cache may be stale or incomplete; for authoritative answers use
 * {@link ServerDiscoveryApi} (server-side).
 */
@ClientOnly
public final class ClientDiscoveryApi {
    private ClientDiscoveryApi() {}

    /**
     * Whether the entity type is discovered by the current local player.
     * Returns {@code false} if the discovery manager is unavailable.
     */
    public static boolean isDiscovered(EntityType<?> type) {
        ClientDiscoveryCacheManager dcm = manager();
        return dcm != null && dcm.isDiscovered(type);
    }

    /**
     * Get the discovery record for the entity type, or {@link Optional#empty()} if undiscovered.
     * Returns {@link Optional#empty()} if the discovery manager is unavailable.
     */
    public static Optional<DiscoveryRecord> getRecord(EntityType<?> type) {
        ClientDiscoveryCacheManager dcm = manager();
        return dcm == null ? Optional.empty() : Optional.ofNullable(dcm.getRecord(type));
    }

    /**
     * Discovery counts of the current local player across all trackable entity types.
     * Returns {@code (0, 0)} if the discovery manager is unavailable.
     */
    public static DiscoveryProgress getProgress() {
        ClientWorldSession cws = ClientWorldSession.get();
        ClientDiscoveryCacheManager dcm = cws != null ? cws.getDiscoveryCacheManager() : null;
        WorldSession ws = WorldSession.get();
        if (dcm == null || ws == null) {
            return new DiscoveryProgress(0, 0);
        }
        var entries = ws.getEntityManager().getEntityEntries();
        int discovered = 0;
        for (var entry : entries) {
            if (dcm.isDiscovered(entry.getType())) { discovered++; }
        }
        return new DiscoveryProgress(discovered, entries.size());
    }

    /**
     * Record a discovery from a third-party {@link DiscoverySource} on the client.
     * The source's {@link DiscoverySource#clientCheck} gate applies; this submits a
     * request to the server, which remains authoritative.
     * Returns {@code false} if the discovery manager is unavailable.
     * @return true if the request was submitted (the server may still reject it)
     */
    public static boolean recordDiscovery(DiscoverySource source, Entity entity) {
        LocalPlayer player = ClientUtils.getClientPlayer();
        if (player == null) { return false; }
        ClientDiscoveryCacheManager dcm = manager();
        return dcm != null && dcm.onDiscoveryEvent(source, player, entity);
    }

    private static ClientDiscoveryCacheManager manager() {
        ClientWorldSession cws = ClientWorldSession.get();
        return cws != null ? cws.getDiscoveryCacheManager() : null;
    }
}
