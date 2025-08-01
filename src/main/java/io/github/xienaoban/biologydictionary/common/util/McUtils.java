package io.github.xienaoban.biologydictionary.common.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class McUtils {
    /**
     * It supports both obfuscated and deobfuscated Minecraft classes.
     */
    public static boolean isVanillaClass(Class<?> clazz) {
        return clazz.getPackageName().startsWith(JavaNames.MINECRAFT_PACKAGE);
    }

    public static boolean isVanillaClass(String clazzName) {
        return clazzName.startsWith(JavaNames.MINECRAFT_PACKAGE);
    }

    public static void showClientTextBoxMessage(ServerPlayer player, Component component) {
        player.displayClientMessage(component, false);
    }

    public static void showClientCenteredMessage(ServerPlayer player, Component component) {
        player.displayClientMessage(component, true);
    }
}
