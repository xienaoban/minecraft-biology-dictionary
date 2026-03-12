package io.github.xienaoban.biologydictionary.platform.client.fabric;

import io.github.xienaoban.biologydictionary.platform.client.ClientEventRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

@Environment(EnvType.CLIENT)
public final class ClientEventRegistryImpl {
    public static void registerStarted(ClientEventRegistry.ClientListener listener) {
        ClientLifecycleEvents.CLIENT_STARTED.register(listener::run);
    }

    public static void registerStopping(ClientEventRegistry.ClientListener listener) {
        ClientLifecycleEvents.CLIENT_STOPPING.register(listener::run);
    }

    public static void registerWorldConnected(ClientEventRegistry.ClientListener listener) {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> listener.run(client));
    }

    public static void registerWorldDisconnecting(ClientEventRegistry.ClientListener listener) {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> listener.run(client));
    }

    public static void registerEndTick(ClientEventRegistry.ClientListener listener) {
        ClientTickEvents.END_CLIENT_TICK.register(listener::run);
    }
}
