package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.platform.Platform;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class ServerNetApi {
    private static final PlatformBridge PB = Platform.load(PlatformBridge.class);

    public static void send(ServerPlayer player, Packet payload) {
        PB.send(player, payload);
    }

    public record Context(MinecraftServer server, ServerPlayer player) {}

    interface PlatformBridge {
        void send(ServerPlayer player, Packet payload);
    }
}
