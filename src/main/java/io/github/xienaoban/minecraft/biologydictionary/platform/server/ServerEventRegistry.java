package io.github.xienaoban.minecraft.biologydictionary.platform.server;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class ServerEventRegistry {
    public static void registerStarted(ServerListener listener) {
        ServerLifecycleEvents.SERVER_STARTED.register(listener::run);
    }

    public static void registerStopping(ServerListener listener) {
        ServerLifecycleEvents.SERVER_STOPPING.register(listener::run);
    }

    @FunctionalInterface
    public interface ServerListener {
        void run(MinecraftServer server);
    }
}
