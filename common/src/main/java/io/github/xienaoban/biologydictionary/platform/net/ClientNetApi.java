package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

@ClientOnly
public final class ClientNetApi {
    private static final PlatformBridge PB = Platform.load(PlatformBridge.class);

    public static void send(Packet payload) {
        PB.send(payload);
    }

    public record Context(Minecraft client, LocalPlayer player) {}

    interface PlatformBridge {
        void send(Packet payload);
    }
}
