package io.github.xienaoban.biologydictionary.platform.util.fabric;

import net.minecraft.server.level.ServerPlayer;

public final class ServerUtilsImpl {
    private ServerUtilsImpl() {}

    public static String getLanguage(ServerPlayer player) {
        // Fabric 1.20.1 does not expose the client language on the server side.
        return "en_us";
    }
}
