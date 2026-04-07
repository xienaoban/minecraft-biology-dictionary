package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.compat.CompatibilityManager;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.biologydictionary.platform.server.ServerEventRegistry;
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

    public static final BiologyDictionary BD = new BiologyDictionary();

    private BiologyDictionary() {
        CompatibilityManager.init();
        EntityUtils.init();
        ServerNetManager.init();
        BiologyDictionaryItem.init();
        EntityProperties.init();
        BiologySkills.init();
        ConfigsManager.load();

        ServerEventRegistry.registerStarted(server -> {
            WorldSession.init(server.getAllLevels().iterator().next());
            ServerWorldSession.init(server);
        });
        ServerEventRegistry.registerStopping(server -> {
            // TODO mov to deinit
            ServerWorldSession.get().getDiscoveryManager().save();
            ServerWorldSession.deinit();
            WorldSession.deinit();
        });

        LOGGER.info("BiologyDictionary initialized.");
    }

    public void forceInitialize() { /* do nothing but to trigger cinit */ }

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
