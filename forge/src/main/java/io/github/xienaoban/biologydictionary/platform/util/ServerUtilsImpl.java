package io.github.xienaoban.biologydictionary.platform.util;

import net.minecraft.server.level.ServerPlayer;

public final class ServerUtilsImpl implements ServerUtils.PlatformBridge {
    @Override
    public String getLanguage(ServerPlayer player) {
        return player.getLanguage();
    }
}
