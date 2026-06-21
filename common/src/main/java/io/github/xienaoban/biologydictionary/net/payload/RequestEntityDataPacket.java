package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.EntityProperty;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

public record RequestEntityDataPacket(int entityId, boolean firstAndFullSync) implements Packet {
    public static final Packet.Factory<RequestEntityDataPacket> FACTORY = RequestEntityDataPacket::new;

    private RequestEntityDataPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(firstAndFullSync);
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        Entity entity = ctx.player().level().getEntity(entityId);

        ReplyEntityDataPacket toSend;
        if (entity != null) {
            CompoundTag vanillaNbt = EntityUtils.getNbt(entity);

            CompoundTag extraNbt = new CompoundTag();
            for (EntityProperty<?> property : new EntityProperties<>(entity).getExtras()) {
                property.getFrom(Misc.cast(entity));
                property.writeTo(extraNbt);
            }

            toSend = new ReplyEntityDataPacket(true, EntityUtils.getId(entity), vanillaNbt, extraNbt);
        } else {
            toSend = new ReplyEntityDataPacket(false, -1, null, null);
        }

        ServerNetApi.send(ctx.player(), toSend);
    }
}
