package io.github.xienaoban.minecraft.biologydictionary.net.payloads;

import io.github.xienaoban.minecraft.biologydictionary.platform.net.PacketPayloadMeta;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequestHandbookItemPacketPayload() implements CustomPacketPayload {
    public static final PacketPayloadMeta<RequestHandbookItemPacketPayload> META = PacketPayloadMeta.create(RequestHandbookItemPacketPayload.class);

    @SuppressWarnings("unused")
    private RequestHandbookItemPacketPayload(FriendlyByteBuf buf) { this(); }

    @SuppressWarnings("unused")
    private void write(FriendlyByteBuf buf) {}

    @Override
    public Type<? extends CustomPacketPayload> type() { return META.type(); }
}
