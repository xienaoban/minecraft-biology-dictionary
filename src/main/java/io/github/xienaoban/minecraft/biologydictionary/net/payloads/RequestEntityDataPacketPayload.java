package io.github.xienaoban.minecraft.biologydictionary.net.payloads;

import io.github.xienaoban.minecraft.biologydictionary.platform.net.PacketPayloadMeta;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record RequestEntityDataPacketPayload(int entityId) implements CustomPacketPayload {
    public static final PacketPayloadMeta<RequestEntityDataPacketPayload> META = PacketPayloadMeta.create(RequestEntityDataPacketPayload.class);

    @SuppressWarnings("unused")
    public RequestEntityDataPacketPayload(FriendlyByteBuf buf) { this(buf.readInt()); }

    @SuppressWarnings("unused")
    public void write(FriendlyByteBuf buf) { buf.writeInt(entityId); }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return META.type(); }
}
