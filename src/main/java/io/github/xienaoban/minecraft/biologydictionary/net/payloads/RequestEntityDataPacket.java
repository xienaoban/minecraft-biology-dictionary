package io.github.xienaoban.minecraft.biologydictionary.net.payloads;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityProperty;
import io.github.xienaoban.minecraft.biologydictionary.common.net.Packet;
import io.github.xienaoban.minecraft.biologydictionary.common.net.PacketPayloadMeta;
import io.github.xienaoban.minecraft.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.minecraft.biologydictionary.common.util.Misc;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

public record RequestEntityDataPacket(int entityId) implements Packet {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public Type<? extends Packet> type() { return META.type(); }

    @SuppressWarnings("unused")
    public RequestEntityDataPacket(FriendlyByteBuf buf) { this(buf.readInt()); }

    @Override
    public void write(FriendlyByteBuf buf) { buf.writeInt(entityId); }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        Entity entity = ctx.player().getCommandSenderWorld().getEntity(entityId);

        SendEntityDataPacket toSend;
        if (entity != null) {
            // Write vanilla NBT data.
            CompoundTag vanillaNbt = entity.saveWithoutId(new CompoundTag());

            // Write data that not in vanilla NBT.
            CompoundTag extraNbt = new CompoundTag();
            for (EntityProperty<?> p : new EntityProperties<>(entity).getExtras()) {
                p.loadFrom(Misc.cast(entity));
                p.writeTo(extraNbt);
            }
            toSend = new SendEntityDataPacket(true, entity.getId(), vanillaNbt, extraNbt);
        } else {
            toSend = new SendEntityDataPacket(false, -1, null, null);
        }

        ServerNetApi.send(ctx.player(), toSend);
    }
}
