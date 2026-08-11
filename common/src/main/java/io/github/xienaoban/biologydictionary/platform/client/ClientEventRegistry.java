package io.github.xienaoban.biologydictionary.platform.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

@ClientOnly
public final class ClientEventRegistry {
    // WORLD_DISCONNECTING is fired by MinecraftMixin on the render thread instead of binding
    // to the platform DISCONNECT events (Fabric `ClientPlayConnectionEvents.DISCONNECT` fires
    // on the network thread from `Connection.channelInactive`, concurrent with the render
    // thread still drawing the final frame of the current screen, so any screen that reads the
    // session can observe a null one and NPE; NeoForge's `LoggingOut` fires on the render
    // thread but still before Minecraft replaces the current screen). The mixin point runs on
    // the render thread and always after `setScreenAndShow` has swapped the screen, so no
    // screen is rendered after teardown.
    private static final List<ClientListener> worldDisconnectingListeners = new ArrayList<>();

    public static void registerWorldDisconnecting(ClientListener listener) {
        worldDisconnectingListeners.add(listener);
    }

    /** Fired by {@code MinecraftMixin} at the tail of {@code Minecraft.updateLevelInEngines} with a null level. */
    public static void fireWorldDisconnecting(Minecraft client) {
        for (ClientListener listener : worldDisconnectingListeners) {
            listener.run(client);
        }
    }

    @ExpectPlatform
    public static void registerStarted(ClientListener listener) { throw new AssertionError(); }

    @ExpectPlatform
    public static void registerStopping(ClientListener listener) { throw new AssertionError(); }

    @ExpectPlatform
    public static void registerWorldConnected(ClientListener listener) { throw new AssertionError(); }

    @ExpectPlatform
    public static void registerEndTick(ClientListener listener) { throw new AssertionError(); }

    @FunctionalInterface
    public interface ClientListener {
        void run(Minecraft client);
    }
}
