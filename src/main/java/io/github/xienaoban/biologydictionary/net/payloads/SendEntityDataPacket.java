package io.github.xienaoban.biologydictionary.net.payloads;

import io.github.xienaoban.biologydictionary.common.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketPayloadMeta;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import static io.github.xienaoban.biologydictionary.BiologyDictionaryClient.BDC;

public record SendEntityDataPacket(boolean notNull, int entityId, CompoundTag vanillaNbt, CompoundTag extraNbt) implements Packet {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public Type<? extends Packet> type() { return META.type(); }

    @SuppressWarnings("unused")
    public SendEntityDataPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readInt(), buf.readNbt(), buf.readNbt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(notNull);
        buf.writeInt(entityId);
        buf.writeNbt(vanillaNbt);
        buf.writeNbt(extraNbt);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        if (!notNull) return;

        Entity entity = BDC.getHitEntity();
        EntityProperties<?> properties = BDC.getHitEntityProperties();
        if (entity == null || entity.getId() != entityId || properties == null) return;
        properties.update(vanillaNbt, extraNbt);
    }
}
