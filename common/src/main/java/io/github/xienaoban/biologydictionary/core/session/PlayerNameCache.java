package io.github.xienaoban.biologydictionary.core.session;

import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Client-side display-name cache for player UUIDs. Local lookups consult the cache and the
 * local connection's player info and never block; {@link #getDisplayNameOrRequest} and
 * {@link #request(Collection)} additionally send a background request once per unknown UUID,
 * storing the UUID as a temporary value until the server's reply overwrites it.
 */
@ClientOnly
public final class PlayerNameCache {
    private final ConcurrentMap<UUID, String> names = new ConcurrentHashMap<>();

    public void putAll(Map<UUID, String> names) {
        this.names.putAll(names);
    }

    public void put(UUID playerId, String name) {
        names.put(playerId, name);
    }

    /**
     * Resolve from the local cache, then the local connection's player info (cached on hit).
     * Returns {@code null} when the name is unknown; never touches the network.
     */
    private String get(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        String cached = names.get(playerId);
        if (cached != null) {
            return cached;
        }
        String local = ClientUtils.getPlayerName(playerId);
        if (local != null) {
            names.put(playerId, local);
        }
        return local;
    }

    /**
     * Display name from the local cache/connection, or the UUID itself; never requests.
     */
    public String getDisplayName(UUID playerId) {
        if (playerId == null) {
            return "";
        }
        String name = get(playerId);
        return name != null ? name : playerId.toString();
    }

    /**
     * Like {@link #getDisplayName}, but requests the name once when unknown. The UUID is
     * stored as a temporary cache value so later calls do not request again; the server's
     * reply overwrites it with the real name.
     */
    public String getDisplayNameOrRequest(UUID playerId) {
        if (playerId == null) {
            return "";
        }
        String name = get(playerId);
        if (name != null) {
            return name;
        }
        boolean[] inserted = {false};
        String value = names.computeIfAbsent(playerId, id -> {
            inserted[0] = true;
            return id.toString();
        });
        if (inserted[0]) {
            ClientNetManager.requestPlayerNames(Set.of(playerId));
        }
        return value;
    }

    /**
     * Request names for the given players in one packet; unknown ones get a temporary UUID
     * cache value so they are requested only once.
     */
    public void request(Collection<UUID> playerIds) {
        Set<UUID> missing = new HashSet<>();
        for (UUID playerId : playerIds) {
            if (playerId != null && get(playerId) == null
                    && names.putIfAbsent(playerId, playerId.toString()) == null) {
                missing.add(playerId);
            }
        }
        if (!missing.isEmpty()) {
            ClientNetManager.requestPlayerNames(missing);
        }
    }
}
