package io.github.xienaoban.biologydictionary.platform.client;

import io.github.xienaoban.biologydictionary.client.ClientEvents;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class ClientEventRegistrar {
    private ClientEventRegistrar() {}

    public static void register() {
        for (ClientEvents.ClientListener listener : ClientEvents.STARTED) {
            NeoForge.EVENT_BUS.addListener((ClientStartedEvent event) -> listener.run(event.getClient()));
        }
        for (ClientEvents.ClientListener listener : ClientEvents.STOPPING) {
            NeoForge.EVENT_BUS.addListener((ClientStoppingEvent event) -> listener.run(event.getClient()));
        }
        for (ClientEvents.ClientListener listener : ClientEvents.WORLD_CONNECTED) {
            NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> listener.run(Minecraft.getInstance()));
        }
        for (ClientEvents.ClientListener listener : ClientEvents.WORLD_DISCONNECTING) {
            NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> listener.run(Minecraft.getInstance()));
        }
        for (ClientEvents.ClientListener listener : ClientEvents.END_TICK) {
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> listener.run(Minecraft.getInstance()));
        }
    }
}
