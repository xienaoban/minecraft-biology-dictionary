package io.github.xienaoban.biologydictionary.platform.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.Minecraft;

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
