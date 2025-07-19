package io.github.xienaoban.biologydictionary.net.payloads;

import io.github.xienaoban.biologydictionary.api.EntityProperty;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketPayloadMeta;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
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
        Entity entity = ctx.player().level().getEntity(entityId);

        SendEntityDataPacket toSend;
        if (entity != null) {
            // Write vanilla NBT data.
            CompoundTag vanillaNbt = EntityUtils.getNbt(entity);

            // Write data that not in vanilla NBT.
            CompoundTag extraNbt = new CompoundTag();
            for (EntityProperty<?> p : new EntityProperties<>(entity).getExtras()) {
                p.getFrom(Misc.cast(entity));
                p.writeTo(extraNbt);
            }
            toSend = new SendEntityDataPacket(true, entity.getId(), vanillaNbt, extraNbt);
        } else {
            toSend = new SendEntityDataPacket(false, -1, null, null);
        }

        ServerNetApi.send(ctx.player(), toSend);
    }
}
