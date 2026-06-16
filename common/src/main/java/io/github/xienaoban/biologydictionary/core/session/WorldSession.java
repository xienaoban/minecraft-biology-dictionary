package io.github.xienaoban.biologydictionary.core.session;

import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsUpdateCallback;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.EntityOverviewCache;
import io.github.xienaoban.biologydictionary.core.property.StaticEntityPropertyCache;
import io.github.xienaoban.biologydictionary.core.skill.SkillCostsCache;
import io.github.xienaoban.biologydictionary.platform.ClientAndServer;
import net.minecraft.world.level.Level;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Manages data structures that are tied to the current world session.
 *
 * <p>TODO: restore the full 1.21.11 implementation after the dependent
 * client session and server session code is ported.</p>
 */
public final class WorldSession implements ConfigsUpdateCallback {
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

    public StaticEntityPropertyCache getStaticEntityPropertyCache() {
        return staticEntityPropertyCache;
    }

    public EntityOverviewCache getEntityOverviewCache() {
        return entityOverviewCache;
    }

    public SkillCostsCache getSkillCostsCache() {
        return skillCostsCache;
    }
}
