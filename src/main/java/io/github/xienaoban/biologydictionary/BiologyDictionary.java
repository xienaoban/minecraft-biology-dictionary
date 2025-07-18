package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.common.server.ServerEventRegistry;
import io.github.xienaoban.biologydictionary.common.util.DevUtils;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class BiologyDictionary {
    public static final String MOD_ID = "biologydictionary";

    public static final String MODRINTH_PAGE = "https://modrinth.com/mod/bole";

    public static final Logger LOGGER = LogManager.getLogger(BiologyDictionary.class);

    public static final BiologyDictionary BD = new BiologyDictionary();

    private final Set<MinecraftServer> servers;

    private BiologyDictionary() {
        this.servers = ConcurrentHashMap.newKeySet();

        ServerNetManager.init();

        ServerEventRegistry.registerStarted(servers::add);
        ServerEventRegistry.registerStopping(servers::remove);

        ServerEventRegistry.registerStarted(server -> EntityManager.init());
        ServerEventRegistry.registerStopping(server -> EntityManager.destroy());

        LOGGER.info("BiologyDictionary initialized.");
    }

    public void forceInitialize() { /* do nothing but to trigger cinit */ }

    public Set<MinecraftServer> getServers() {
        return servers;
    }

    public Level justGiveMeALevel() {
        if (DevUtils.isClient()) {
            Level level = McClientUtils.getClientLevel0();
            if (level != null) { return level; }
        }
        for (MinecraftServer server : getServers()) {
            for (Level level : server.getAllLevels()) {
                return level;
            }
        }
        return null;
    }
}
