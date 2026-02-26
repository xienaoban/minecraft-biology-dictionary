package io.github.xienaoban.biologydictionary.common.server;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.MinecraftServer;

public final class ServerEventRegistry {
    @ExpectPlatform
    public static void registerStarted(ServerListener listener) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerStopping(ServerListener listener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface ServerListener {
        void run(MinecraftServer server);
    }
}