package io.github.xienaoban.biologydictionary.platform.client;

import io.github.xienaoban.biologydictionary.client.ClientEvents;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

public final class ClientEventRegistrar {
    private ClientEventRegistrar() {}

    public static void register() {
        // Forge 1.20.1 has no client started / stopping events on the game event bus, so
        // ClientEvents.STARTED and ClientEvents.STOPPING are not bound here. They are empty on
        // this version.
        for (ClientEvents.ClientListener listener : ClientEvents.WORLD_CONNECTED) {
            MinecraftForge.EVENT_BUS.addListener(
                    (ClientPlayerNetworkEvent.LoggingIn event) -> listener.run(ClientUtils.getClient()));
        }
        // WORLD_DISCONNECTING is fired by MinecraftMixin instead of binding to Forge's
        // LoggingOut, so both platforms share the same render-thread timing.
        // for (ClientEvents.ClientListener listener : ClientEvents.WORLD_DISCONNECTING) {
        //     MinecraftForge.EVENT_BUS.addListener(
        //             (ClientPlayerNetworkEvent.LoggingOut event) -> listener.run(ClientUtils.getClient()));
        // }
        for (ClientEvents.ClientListener listener : ClientEvents.END_TICK) {
            MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
                if (event.phase == TickEvent.Phase.END) {
                    listener.run(ClientUtils.getClient());
                }
            });
        }
    }
}
