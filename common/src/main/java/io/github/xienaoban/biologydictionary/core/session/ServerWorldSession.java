package io.github.xienaoban.biologydictionary.core.session;

import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsUpdateCallback;
import io.github.xienaoban.biologydictionary.core.EntitySpawnManager;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryManager;
import net.minecraft.server.MinecraftServer;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Server-side data tied to the current world session.
 * Created when the server starts, destroyed when the server stops.
 */
public final class ServerWorldSession implements ConfigsUpdateCallback {
    private static volatile ServerWorldSession instance;

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

    private final DiscoveryManager discoveryManager;
    private final EntitySpawnManager entitySpawnManager;

    private ServerWorldSession(MinecraftServer server) {
        this.server = server;
        this.discoveryManager = new DiscoveryManager(server);
        this.entitySpawnManager = new EntitySpawnManager(server.registryAccess(), server.getStructureManager());
    }

    @Override
    public void onConfigsUpdate(Configs.ClientConfigs clientConfigs, Configs.ServerConfigs serverConfigs) {
        discoveryManager.onConfigsUpdate(clientConfigs, serverConfigs);
    }

    public MinecraftServer getServer() {
        return server;
    }

    public DiscoveryManager getDiscoveryManager() {
        return discoveryManager;
    }

    public EntitySpawnManager getEntitySpawnManager() {
        return entitySpawnManager;
    }
}
