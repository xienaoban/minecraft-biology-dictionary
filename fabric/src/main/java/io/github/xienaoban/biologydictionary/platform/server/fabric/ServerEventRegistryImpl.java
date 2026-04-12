package io.github.xienaoban.biologydictionary.platform.server.fabric;

import io.github.xienaoban.biologydictionary.platform.server.ServerEventRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class ServerEventRegistryImpl {
    public static void registerStarted(ServerEventRegistry.ServerListener listener) {
        ServerLifecycleEvents.SERVER_STARTED.register(listener::run);
    }

    public static void registerStopping(ServerEventRegistry.ServerListener listener) {
        ServerLifecycleEvents.SERVER_STOPPING.register(listener::run);
    }

    public static void registerPlayerLoggedIn(ServerEventRegistry.PlayerListener listener) {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> listener.run(handler.getPlayer()));
    }
}
