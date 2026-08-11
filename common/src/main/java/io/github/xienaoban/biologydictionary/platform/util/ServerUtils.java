package io.github.xienaoban.biologydictionary.platform.util;

import io.github.xienaoban.biologydictionary.platform.ClientAndServer;
import net.minecraft.server.MinecraftServer;

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
}
