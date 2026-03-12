package io.github.xienaoban.biologydictionary.platform.net;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class ServerNetApi {

    @ExpectPlatform
    public static <T extends Packet> void register(Class<T> clazz, Packet.Factory<T> factory) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void send(ServerPlayer player, Packet payload) {
        throw new AssertionError();
    }

    public record Context(MinecraftServer server, ServerPlayer player) {}
}
