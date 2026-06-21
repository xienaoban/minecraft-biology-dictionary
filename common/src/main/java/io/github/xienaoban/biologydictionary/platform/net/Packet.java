package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface Packet extends CustomPacketPayload {
    void write(FriendlyByteBuf buf);

    @ClientOnly
    default void clientReceive(ClientNetApi.Context ctx) {
        throw new AssertionError();
    }

    default void serverReceive(ServerNetApi.Context ctx) {
        throw new AssertionError();
    }

    @Override
    default CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PacketUtil.getType(getClass());
    }

    @FunctionalInterface
    interface Factory<T extends Packet> {
        T create(FriendlyByteBuf buf);
    }
}
