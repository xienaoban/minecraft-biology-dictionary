package io.github.xienaoban.minecraft.biologydictionary.net.payloads;

import io.github.xienaoban.minecraft.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.minecraft.biologydictionary.platform.net.PacketPayloadMeta;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;
import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionaryClient.BDC;

public record SendEntityDataPacketPayload(boolean notNull, int entityId, CompoundTag vanillaNbt, CompoundTag additionalNbt) implements CustomPacketPayload {
    public static final PacketPayloadMeta<SendEntityDataPacketPayload> META = PacketPayloadMeta.create(SendEntityDataPacketPayload.class);

    @SuppressWarnings("unused")
    public SendEntityDataPacketPayload(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readInt(), buf.readNbt(), buf.readNbt());
    }

    @SuppressWarnings("unused")
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(notNull);
        buf.writeInt(entityId);
        buf.writeNbt(vanillaNbt);
        buf.writeNbt(additionalNbt);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return META.type(); }

    @SuppressWarnings("unused")
    public void clientReceive(ClientNetApi.Context ctx) {
        if (!notNull) return;
        Entity entity = BDC.getHitEntity();
        if (entity == null || entity.getId() != entityId) return;
        LOGGER.info("vanillaNbt = " + vanillaNbt);
        LOGGER.info("additionalNbt = " + additionalNbt);
    }
}
