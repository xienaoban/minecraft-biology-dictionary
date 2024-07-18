package io.github.xienaoban.minecraft.biologydictionary.net.payloads;

import io.github.xienaoban.minecraft.biologydictionary.platform.net.PacketPayloadMeta;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SendEntityDataPacketPayload(boolean notNull, int entityId, CompoundTag vanillaNbt, CompoundTag additionalNbt) implements CustomPacketPayload {
    public static final PacketPayloadMeta<SendEntityDataPacketPayload> META = PacketPayloadMeta.create(SendEntityDataPacketPayload.class);

    @SuppressWarnings("unused")
    private SendEntityDataPacketPayload(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readInt(), buf.readNbt(), buf.readNbt());
    }

    @SuppressWarnings("unused")
    private void write(FriendlyByteBuf buf) {
        buf.writeBoolean(notNull);
        buf.writeInt(entityId);
        buf.writeNbt(vanillaNbt);
        buf.writeNbt(additionalNbt);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return META.type(); }
}
