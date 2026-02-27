package io.github.xienaoban.biologydictionary.common.client.neoforge;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import io.github.xienaoban.biologydictionary.common.client.ClientEventRegistry;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientEventRegistryImpl {

    public static void registerStarted(ClientEventRegistry.ClientListener listener) {
        ClientLifecycleEvent.CLIENT_STARTED.register(client -> listener.run(client));
    }

    public static void registerStopping(ClientEventRegistry.ClientListener listener) {
        ClientLifecycleEvent.CLIENT_STOPPING.register(client -> listener.run(client));
    }

    public static void registerWorldConnected(ClientEventRegistry.ClientListener listener) {
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> listener.run(Minecraft.getInstance()));
    }

    public static void registerWorldDisconnecting(ClientEventRegistry.ClientListener listener) {
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> listener.run(Minecraft.getInstance()));
    }

    public static void registerEndTick(ClientEventRegistry.ClientListener listener) {
        ClientTickEvent.CLIENT_POST.register(client -> listener.run(client));
    }
}
