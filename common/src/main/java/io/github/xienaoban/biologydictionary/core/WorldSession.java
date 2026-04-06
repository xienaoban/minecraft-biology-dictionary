package io.github.xienaoban.biologydictionary.core;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryManager;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.DevUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.Iterator;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Manages data structures that are tied to the current world session.
 * A session starts when a world is created/joined and ends when leaving.
 * On integrated servers (single player), both client and server share the same JVM
 * instance, so only one WorldSession is created.
 */
public final class WorldSession {
    private static volatile WorldSession instance;

    public static void init(MinecraftServer server) {
        synchronized (WorldSession.class) {
            if (instance == null) {
                instance = new WorldSession(server);
                LOGGER.info("WorldSession initialized.");
            }
        }
    }

    public static void deinit(MinecraftServer server) {
        synchronized (WorldSession.class) {
            if (instance != null) {
                instance = null;
                LOGGER.info("WorldSession deinitialized.");
            }
        }
    }

    public static WorldSession get() {
        return instance;
    }

    /**
     * Get a level instance. Any level is OK. Usually be used in {@code EntityType.create}.
     */
    public static Level justGiveMeALevel() {
        return justGiveMeALevel(instance.server);
    }

    private static Level justGiveMeALevel(MinecraftServer server) {
        if (DevUtils.isClient()) {
            Level level = ClientUtils.getClientLevelCommon();
            if (level != null) { return level; }
        }
        if (server != null) {
            Iterator<? extends Level> it = server.getAllLevels().iterator();
            if (it.hasNext()) { return it.next(); }
        }
        return null;
    }

    private final MinecraftServer server;
    private final EntityManager entityManager;
    private final EntityOverviewCache entityOverviewCache;
    private final EntitySpawnManager entitySpawnManager;
    private final DiscoveryManager discoveryManager;

    private WorldSession(MinecraftServer server) {
        this.server = server;
        this.entityManager = EntityManager.create(justGiveMeALevel(server));
        this.entityOverviewCache = new EntityOverviewCache();
        this.entitySpawnManager = server != null
            ? new EntitySpawnManager(server.registryAccess())
            : null;
        this.discoveryManager = server != null ? new DiscoveryManager(server) : null;
    }

    public MinecraftServer getServer() {
        return server;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public EntityOverviewCache getEntityOverviewCache() {
        return entityOverviewCache;
    }

    public EntitySpawnManager getEntitySpawnManager() {
        return entitySpawnManager;
    }

    public DiscoveryManager getDiscoveryManager() {
        return discoveryManager;
    }
}
