package io.github.xienaoban.biologydictionary.common.net;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface Packet extends CustomPacketPayload {
    void write(FriendlyByteBuf buf);

    @Environment(EnvType.CLIENT)
    default void clientReceive(ClientNetApi.Context ctx) {
        throw new AssertionError("Not implemented!");
    }

    default void serverReceive(ServerNetApi.Context ctx) {
        throw new AssertionError("Not implemented!");
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
