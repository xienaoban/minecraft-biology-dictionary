package io.github.xienaoban.biologydictionary.platform.client;

import io.github.xienaoban.biologydictionary.client.ClientEvents;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class ClientEventRegistrar {
    private ClientEventRegistrar() {}

    public static void register() {
        // NeoForge 21.1 has no ClientStartedEvent / ClientStoppingEvent, so ClientEvents.STARTED and
        // ClientEvents.STOPPING are not bound here. They are empty on this version.
        for (ClientEvents.ClientListener listener : ClientEvents.WORLD_CONNECTED) {
            NeoForge.EVENT_BUS.addListener(
                    (ClientPlayerNetworkEvent.LoggingIn event) -> listener.run(ClientUtils.getClient()));
        }
        // WORLD_DISCONNECTING is fired by MinecraftMixin instead of binding to NeoForge's
        // LoggingOut, so both platforms share the same render-thread timing.
        // for (ClientEvents.ClientListener listener : ClientEvents.WORLD_DISCONNECTING) {
        //     NeoForge.EVENT_BUS.addListener(
        //             (ClientPlayerNetworkEvent.LoggingOut event) -> listener.run(ClientUtils.getClient()));
        // }
        for (ClientEvents.ClientListener listener : ClientEvents.END_TICK) {
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> listener.run(ClientUtils.getClient()));
        }
    }
}
