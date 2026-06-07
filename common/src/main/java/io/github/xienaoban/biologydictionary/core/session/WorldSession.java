package io.github.xienaoban.biologydictionary.core.session;

import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsUpdateCallback;
import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.EntityOverviewCache;
import io.github.xienaoban.biologydictionary.core.property.StaticEntityPropertyCache;
import io.github.xienaoban.biologydictionary.core.skill.SkillCostsCache;
import io.github.xienaoban.biologydictionary.platform.ClientAndServer;
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
public final class WorldSession implements ConfigsUpdateCallback {
    private static volatile WorldSession instance;

    @ClientAndServer
    public static void init(Level level) {
        synchronized (WorldSession.class) {
            if (instance == null) {
                try {
                    instance = new WorldSession(level);
                    LOGGER.info("WorldSession initialized.");
                } catch (Throwable e) {
                    LOGGER.error("Failed to initialize WorldSession!", e);
                    if (DevUtils.isClient()) {
                        BiologyDictionaryClient.printThrowableToLoggerAndGame("WorldSession failed to initialize!", e);
                    }
                }
            }
        }
    }

    public static void deinit() {
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
    @ClientAndServer
    public static Level justGiveMeALevel() {
        if (DevUtils.isClient()) {
            Level level = ClientUtils.getClientLevelCommon();
            if (level != null) { return level; }
        }
        ServerWorldSession serverSession = ServerWorldSession.get();
        if (serverSession != null) {
            MinecraftServer server = serverSession.getServer();
            Iterator<? extends Level> it = server.getAllLevels().iterator();
            if (it.hasNext()) { return it.next(); }
        }
        return null;
    }

    private final EntityManager entityManager;
    private final SkillCostsCache skillCostsCache;
    private final EntityOverviewCache entityOverviewCache;
    private final StaticEntityPropertyCache staticEntityPropertyCache;

    private WorldSession(Level level) {
        this.entityManager = new EntityManager(level);
        this.skillCostsCache = new SkillCostsCache();
        this.entityOverviewCache = new EntityOverviewCache();
        this.staticEntityPropertyCache = new StaticEntityPropertyCache();
    }

    @Override
    public void onConfigsUpdate(Configs.ClientConfigs clientConfigs, Configs.ServerConfigs serverConfigs) {
        skillCostsCache.onConfigsUpdate(clientConfigs, serverConfigs);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public SkillCostsCache getSkillCostsCache() {
        return skillCostsCache;
    }

    public EntityOverviewCache getEntityOverviewCache() {
        return entityOverviewCache;
    }

    public StaticEntityPropertyCache getStaticEntityPropertyCache() {
        return staticEntityPropertyCache;
    }
}
