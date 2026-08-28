package io.github.xienaoban.biologydictionary.platform.util;

import io.github.xienaoban.biologydictionary.platform.ClientAndServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.Optional;
import java.util.UUID;

/**
 * Server-side helpers abstracting APIs that change between MC versions.
 */
public final class ServerUtils {
    private ServerUtils() {}

    /**
     * Whether the server is a dedicated (non-integrated) server.
     */
    @ClientAndServer
    public static boolean isDedicated(MinecraftServer server) {
        return server.isDedicatedServer();
    }

    /**
     * Whether the server is open to other players. A dedicated server always
     * returns true; an integrated server only returns true once LAN publishing
     * has been enabled.
     */
    @ClientAndServer
    public static boolean isMultiplayerOpen(MinecraftServer server) {
        return server.isPublished();
    }

    /**
     * Resolve a player's display name from the online player list, then the server's name cache.
     */
    @ClientAndServer
    public static Optional<String> getPlayerName(MinecraftServer server, UUID playerId) {
        if (playerId == null) {
            return Optional.empty();
        }
        ServerPlayer online = server.getPlayerList().getPlayer(playerId);
        if (online != null) {
            return Optional.of(online.getGameProfile().name());
        }
        return server.services().nameToIdCache().get(playerId).map(NameAndId::name);
    }
}
