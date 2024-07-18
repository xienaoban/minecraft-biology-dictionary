package io.github.xienaoban.minecraft.biologydictionary.net.payloads;

import io.github.xienaoban.minecraft.biologydictionary.platform.net.PacketPayloadMeta;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequestEntityDataPacketPayload(int entityId) implements CustomPacketPayload {
    public static final PacketPayloadMeta<RequestEntityDataPacketPayload> META = PacketPayloadMeta.create(RequestEntityDataPacketPayload.class);

    @SuppressWarnings("unused")
    private RequestEntityDataPacketPayload(FriendlyByteBuf buf) { this(buf.readInt()); }

    @SuppressWarnings("unused")
    private void write(FriendlyByteBuf buf) { buf.writeInt(entityId); }

    @Override
    public Type<? extends CustomPacketPayload> type() { return META.type(); }
}
