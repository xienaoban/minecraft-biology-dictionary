package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.platform.Platform;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class ServerNetApi {
    private static final PlatformBridge PB = Platform.load(PlatformBridge.class);

    public static void send(ServerPlayer player, Packet payload) {
        PB.send(player, payload);
    }

    public static boolean canSend(ServerPlayer player, Class<? extends Packet> packetClass) {
        return PB.canSend(player, packetClass);
    }

    public record Context(MinecraftServer server, ServerPlayer player) {}

    interface PlatformBridge {
        void send(ServerPlayer player, Packet payload);

        boolean canSend(ServerPlayer player, Class<? extends Packet> packetClass);
    }
}
