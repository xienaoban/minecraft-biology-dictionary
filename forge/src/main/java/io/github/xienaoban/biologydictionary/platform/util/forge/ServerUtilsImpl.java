package io.github.xienaoban.biologydictionary.platform.util.forge;

import net.minecraft.server.level.ServerPlayer;

@SuppressWarnings("unused")
public final class ServerUtilsImpl {
    private ServerUtilsImpl() {}

    public static String getLanguage(ServerPlayer player) {
        return player.getLanguage();
    }
}
