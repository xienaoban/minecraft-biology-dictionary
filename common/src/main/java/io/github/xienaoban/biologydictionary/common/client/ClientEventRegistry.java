package io.github.xienaoban.biologydictionary.common.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class ClientEventRegistry {
    @ExpectPlatform
    public static void registerStarted(ClientListener listener) { throw new AssertionError(); }

    @ExpectPlatform
    public static void registerStopping(ClientListener listener) { throw new AssertionError(); }

    @ExpectPlatform
    public static void registerWorldConnected(ClientListener listener) { throw new AssertionError(); }

    @ExpectPlatform
    public static void registerWorldDisconnecting(ClientListener listener) { throw new AssertionError(); }

    @ExpectPlatform
    public static void registerEndTick(ClientListener listener) { throw new AssertionError(); }

    @FunctionalInterface
    public interface ClientListener {
        void run(Minecraft client);
    }
}
