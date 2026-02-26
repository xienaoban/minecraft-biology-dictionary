package io.github.xienaoban.biologydictionary.common.server.fabric;

import io.github.xienaoban.biologydictionary.common.server.ServerEventRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class ServerEventRegistryImpl {
    public static void registerStarted(ServerEventRegistry.ServerListener listener) {
        ServerLifecycleEvents.SERVER_STARTED.register(listener::run);
    }

    public static void registerStopping(ServerEventRegistry.ServerListener listener) {
        ServerLifecycleEvents.SERVER_STOPPING.register(listener::run);
    }
}