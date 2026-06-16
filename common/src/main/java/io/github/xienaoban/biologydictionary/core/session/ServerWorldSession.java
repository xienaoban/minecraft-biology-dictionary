package io.github.xienaoban.biologydictionary.core.session;

import io.github.xienaoban.biologydictionary.core.EntitySpawnManager;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryManager;
import io.github.xienaoban.biologydictionary.platform.ClientAndServer;
import net.minecraft.server.MinecraftServer;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Server-side data tied to the current world session.
 */
public final class ServerWorldSession {
    private static volatile ServerWorldSession instance;

    @ClientAndServer
    public static void init(MinecraftServer server) {
        synchronized (ServerWorldSession.class) {
            if (instance == null) {
                instance = new ServerWorldSession(server);
                LOGGER.info("ServerWorldSession initialized.");
            }
        }
    }

    public static void deinit() {
        synchronized (ServerWorldSession.class) {
            if (instance != null) {
                instance = null;
                LOGGER.info("ServerWorldSession deinitialized.");
            }
        }
    }

    public static ServerWorldSession get() {
        return instance;
    }

    private final MinecraftServer server;
    private final EntitySpawnManager entitySpawnManager;
    private final DiscoveryManager discoveryManager;

    private ServerWorldSession(MinecraftServer server) {
        this.server = server;
        this.entitySpawnManager = new EntitySpawnManager(server.registryAccess(), server.getResourceManager());
        this.discoveryManager = new DiscoveryManager(server);
    }

    public MinecraftServer getServer() {
        return server;
    }

    public EntitySpawnManager getEntitySpawnManager() {
        return entitySpawnManager;
    }

    public DiscoveryManager getDiscoveryManager() {
        return discoveryManager;
    }
}
