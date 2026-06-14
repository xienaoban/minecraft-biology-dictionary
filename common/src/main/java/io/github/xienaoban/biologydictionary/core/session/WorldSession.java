package io.github.xienaoban.biologydictionary.core.session;

import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.property.StaticEntityPropertyCache;
import io.github.xienaoban.biologydictionary.platform.ClientAndServer;
import net.minecraft.world.level.Level;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Manages data structures that are tied to the current world session.
 *
 * <p>TODO: restore the full 1.21.11 implementation after the dependent
 * skill, overview, client session and server session code is ported.</p>
 */
public final class WorldSession {
    private static volatile WorldSession instance;

    @ClientAndServer
    public static void init(Level level) {
        synchronized (WorldSession.class) {
            if (instance == null) {
                instance = new WorldSession(level);
                LOGGER.info("WorldSession initialized.");
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

    private final EntityManager entityManager;
    private final StaticEntityPropertyCache staticEntityPropertyCache;
    // TODO: restore SkillCostsCache and EntityOverviewCache after their dependencies are ported.

    private WorldSession(Level level) {
        this.entityManager = new EntityManager(level);
        this.staticEntityPropertyCache = new StaticEntityPropertyCache();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public StaticEntityPropertyCache getStaticEntityPropertyCache() {
        return staticEntityPropertyCache;
    }
    // TODO: restore getSkillCostsCache() and getEntityOverviewCache().
}
