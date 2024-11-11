package io.github.xienaoban.minecraft.biologydictionary;

import io.github.xienaoban.minecraft.biologydictionary.core.EntityManager;
import io.github.xienaoban.minecraft.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.minecraft.biologydictionary.platform.server.ServerEventRegistry;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BiologyDictionary {
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
        ServerEventRegistry.registerStopping(server -> EntityManager.deinit());

        LOGGER.info("BiologyDictionary initialized.");
    }

    public void forceInitialize() { /* do nothing but to trigger cinit */ }

    public Set<MinecraftServer> getServers() {
        return servers;
    }
}
