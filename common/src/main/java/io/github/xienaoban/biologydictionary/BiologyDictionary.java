package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.EntityOrder;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySources;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BiologyDictionary {
    public static final String MOD_ID = "biologydictionary";

    public static final String MODRINTH_PAGE = "https://modrinth.com/mod/biology-dictionary";
    public static final String CURSEFORGE_PAGE = "https://www.curseforge.com/minecraft/mc-mods/biology-dictionary";
    public static final String GITHUB_PAGE = "https://github.com/xienaoban/minecraft-biology-dictionary";

    public static final Logger LOGGER = LogManager.getLogger(BiologyDictionary.class);

    static {
        EntityUtils.init();
        EntityOrder.init();
        EntityProperties.init();
        BiologySkills.init();
        DiscoverySources.init();
        ConfigsManager.load();
        LOGGER.info("BiologyDictionary initialized.");
    }

    private BiologyDictionary() {}

    public static void forceInitialize() { /* do nothing but to trigger cinit */ }

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
