package io.github.xienaoban.minecraft.biologydictionary.common.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class ClientEventRegistry {
    public static void registerStarted(ClientListener listener) {
        ClientLifecycleEvents.CLIENT_STARTED.register(listener::run);
    }

    public static void registerStopping(ClientListener listener) {
        ClientLifecycleEvents.CLIENT_STOPPING.register(listener::run);
    }

    public static void registerWorldConnected(ClientListener listener) {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> listener.run(client));
    }

    public static void registerWorldDisconnecting(ClientListener listener) {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> listener.run(client));
    }

    public static void registerEndTick(ClientListener listener) {
        ClientTickEvents.END_CLIENT_TICK.register(listener::run);
    }

    @FunctionalInterface
    public interface ClientListener {
        void run(Minecraft client);
    }
}
