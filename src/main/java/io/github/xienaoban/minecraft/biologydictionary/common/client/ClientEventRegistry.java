package io.github.xienaoban.minecraft.biologydictionary.common.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class ClientEventRegistry {
    public static void registerEndTick(ClientListener listener) {
        ClientTickEvents.END_CLIENT_TICK.register(listener::run);
    }

    @FunctionalInterface
    public interface ClientListener {
        void run(Minecraft client);
    }
}
