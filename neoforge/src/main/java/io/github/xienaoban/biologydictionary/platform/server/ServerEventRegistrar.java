package io.github.xienaoban.biologydictionary.platform.server;

import io.github.xienaoban.biologydictionary.server.ServerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public final class ServerEventRegistrar {
    private ServerEventRegistrar() {}

    public static void register() {
        for (ServerEvents.ServerListener listener : ServerEvents.STARTED) {
            NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> listener.run(event.getServer()));
        }
        for (ServerEvents.ServerListener listener : ServerEvents.STOPPING) {
            NeoForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> listener.run(event.getServer()));
        }
        for (ServerEvents.PlayerListener listener : ServerEvents.PLAYER_LOGGED_IN) {
            NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
                if (event.getEntity() instanceof ServerPlayer player) {
                    listener.run(player);
                }
            });
        }
    }
}
