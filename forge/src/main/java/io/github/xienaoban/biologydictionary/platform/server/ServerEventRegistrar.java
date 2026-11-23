package io.github.xienaoban.biologydictionary.platform.server;

import io.github.xienaoban.biologydictionary.platform.net.LegacyClientNotice;
import io.github.xienaoban.biologydictionary.server.ServerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;

public final class ServerEventRegistrar {
    private ServerEventRegistrar() {}

    public static void register() {
        for (ServerEvents.ServerListener listener : ServerEvents.STARTED) {
            MinecraftForge.EVENT_BUS.addListener((ServerStartedEvent event) -> listener.run(event.getServer()));
        }
        for (ServerEvents.ServerListener listener : ServerEvents.STOPPING) {
            MinecraftForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> listener.run(event.getServer()));
        }
        for (ServerEvents.PlayerListener listener : ServerEvents.PLAYER_LOGGED_IN) {
            MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
                if (event.getEntity() instanceof ServerPlayer player) {
                    listener.run(player);
                }
            });
        }
        // Architectury-era peers never answer the manifest, so they get a vanilla chat message.
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                LegacyClientNotice.notifyIfIncompatible(player);
            }
        });
    }
}
