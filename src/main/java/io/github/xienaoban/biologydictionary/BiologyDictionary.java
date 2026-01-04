package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.common.server.ServerEventRegistry;
import io.github.xienaoban.biologydictionary.common.util.DevUtils;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.PlayerSkills;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
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

    public static final String MODRINTH_PAGE = "https://modrinth.com/mod/bole";

    public static final Logger LOGGER = LogManager.getLogger(BiologyDictionary.class);

    public static final BiologyDictionary BD = new BiologyDictionary();

    private final Set<MinecraftServer> servers;

    private BiologyDictionary() {
        servers = ConcurrentHashMap.newKeySet();

        ConfigsManager.load();
        EntityUtils.init();
        ServerNetManager.init();
        BiologyDictionaryItem.init();
        EntityProperties.init();
        PlayerSkills.init();

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
            Level level = ClientUtils.getClientLevel0();
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
        ServerNetManager.sendCenteredMessage(player, text);
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
