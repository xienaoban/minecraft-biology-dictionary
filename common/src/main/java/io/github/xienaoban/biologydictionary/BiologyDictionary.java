package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.platform.server.ServerEventRegistry;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.DevUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class BiologyDictionary {
    public static final String MOD_ID = "biologydictionary";

    public static final String MODRINTH_PAGE = "https://modrinth.com/mod/biology-dictionary";
    public static final String CURSEFORGE_PAGE = "https://www.curseforge.com/minecraft/mc-mods/biology-dictionary";
    public static final String GITHUB_PAGE = "https://github.com/xienaoban/minecraft-biology-dictionary";

    public static final Logger LOGGER = LogManager.getLogger(BiologyDictionary.class);

    public static final BiologyDictionary BD = new BiologyDictionary();

    private final Set<MinecraftServer> servers;

    private BiologyDictionary() {
        servers = ConcurrentHashMap.newKeySet();

        EntityUtils.init();
        // TODO
        // ServerNetManager.init();
        BiologyDictionaryItem.init();
        EntityProperties.init();
        BiologySkills.init();
        ConfigsManager.load();

        ServerEventRegistry.registerStarted(servers::add);
        ServerEventRegistry.registerStopping(servers::remove);

        // TODO
        // ServerEventRegistry.registerStarted(server -> EntityManager.init());
        // ServerEventRegistry.registerStopping(server -> EntityManager.destroy());

        LOGGER.info("BiologyDictionary initialized.");
    }

    public void forceInitialize() { /* do nothing but to trigger cinit */ }

    public Set<MinecraftServer> getServers() {
        return servers;
    }

    /**
     * Get a server instance. Any server is OK. Usually be used in {@code EntityType.create}.
     */
    public Level justGiveMeALevel() {
        if (DevUtils.isClient()) {
            Level level = ClientUtils.getClientLevelCommon();
            if (level != null) { return level; }
        }
        for (MinecraftServer server : getServers()) {
            for (Level level : server.getAllLevels()) {
                return level;
            }
        }
        return null;
    }

    public static void sendCenteredMessage(ServerPlayer player, Component text) {
        // TODO
        // ServerNetManager.sendCenteredMessage(player, text);
    }

    public static void sendCenteredInfo(ServerPlayer player, MutableComponent text) {
        sendCenteredMessage(player, text.withStyle(ChatFormatting.WHITE));
    }

    public static void sendCenteredWarning(ServerPlayer player, MutableComponent text) {
        sendCenteredMessage(player, text.withStyle(ChatFormatting.YELLOW));
    }

    public static void sendCenteredError(ServerPlayer player, MutableComponent text) {
        sendCenteredMessage(player, text.withStyle(ChatFormatting.RED));
    }
}
