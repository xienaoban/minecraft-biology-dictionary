package io.github.xienaoban.biologydictionary.platform.net;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

@ClientOnly
public final class ClientNetApi {

    @ExpectPlatform
    public static <T extends Packet> void register(Class<T> clazz, Packet.Factory<T> factory) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void send(Packet payload) {
        throw new AssertionError();
    }

    public record Context(Minecraft client, LocalPlayer player) {}
}
