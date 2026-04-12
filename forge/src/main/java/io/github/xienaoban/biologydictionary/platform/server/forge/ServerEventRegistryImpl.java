package io.github.xienaoban.biologydictionary.platform.server.forge;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import io.github.xienaoban.biologydictionary.platform.server.ServerEventRegistry;

@SuppressWarnings("unused")
public final class ServerEventRegistryImpl {

    public static void registerStarted(ServerEventRegistry.ServerListener listener) {
        LifecycleEvent.SERVER_STARTED.register(listener::run);
    }

    public static void registerStopping(ServerEventRegistry.ServerListener listener) {
        LifecycleEvent.SERVER_STOPPING.register(listener::run);
    }

    public static void registerPlayerLoggedIn(ServerEventRegistry.PlayerListener listener) {
        PlayerEvent.PLAYER_JOIN.register(player -> listener.run(player));
    }
}
