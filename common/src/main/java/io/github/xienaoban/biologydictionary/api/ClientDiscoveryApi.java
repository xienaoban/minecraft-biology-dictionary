package io.github.xienaoban.biologydictionary.api;

import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCacheManager;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.List;
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
     * Discovered entity entries of the current local player, filtered from the given entries
     * (e.g. the result of {@link EntityInfoApi#getTotalEntities()}).
     * Returns an empty list if the discovery manager is unavailable.
     */
    public static List<EntityDictionaryEntry> getDiscoveredEntities(List<EntityDictionaryEntry> entries) {
        ClientWorldSession cws = ClientWorldSession.get();
        ClientDiscoveryCacheManager dcm = cws != null ? cws.getDiscoveryCacheManager() : null;
        if (dcm == null) { return List.of(); }
        return entries.stream()
                .filter(entry -> dcm.isDiscovered(entry.getType()))
                .toList();
    }

    /**
     * Record a discovery from a third-party {@link DiscoverySource} on the client.
     * The source's {@link DiscoverySource#clientCheck} gate applies; this submits a
     * request to the server, which remains authoritative.
     * <p>
     * Note the return semantics differ from {@link ServerDiscoveryApi#recordDiscovery}:
     * here {@code true} only means the request was submitted — the server may still
     * reject it (server-side validation, strategy, config).
     * Returns {@code false} if the discovery manager is unavailable.
     *
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
