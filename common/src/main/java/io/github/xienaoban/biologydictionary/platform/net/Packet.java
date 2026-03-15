package io.github.xienaoban.biologydictionary.platform.net;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;

public interface Packet {
    void write(FriendlyByteBuf buf);

    @Environment(EnvType.CLIENT)
    default void clientReceive(ClientNetApi.Context ctx) {
        throw new AssertionError();
    }

    default void serverReceive(ServerNetApi.Context ctx) {
        throw new AssertionError();
    }

    @FunctionalInterface
    interface Factory<T extends Packet> {
        T create(FriendlyByteBuf buf);
    }
}
