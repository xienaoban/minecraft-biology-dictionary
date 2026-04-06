package io.github.xienaoban.biologydictionary.platform.server;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class ServerEventRegistry {
    @ExpectPlatform
    public static void registerStarted(ServerListener listener) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerStopping(ServerListener listener) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerPlayerLoggedIn(PlayerListener listener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface ServerListener {
        void run(MinecraftServer server);
    }

    @FunctionalInterface
    public interface PlayerListener {
        void run(ServerPlayer player);
    }
}
