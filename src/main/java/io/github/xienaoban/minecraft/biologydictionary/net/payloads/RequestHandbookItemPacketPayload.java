package io.github.xienaoban.minecraft.biologydictionary.net.payloads;

import io.github.xienaoban.minecraft.biologydictionary.platform.net.PacketPayloadMeta;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record RequestHandbookItemPacketPayload() implements CustomPacketPayload {
    public static final PacketPayloadMeta<RequestHandbookItemPacketPayload> META = PacketPayloadMeta.create(RequestHandbookItemPacketPayload.class);

    @SuppressWarnings("unused")
    public RequestHandbookItemPacketPayload(FriendlyByteBuf buf) { this(); }

    @SuppressWarnings("unused")
    public void write(FriendlyByteBuf buf) {}

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return META.type(); }
}
