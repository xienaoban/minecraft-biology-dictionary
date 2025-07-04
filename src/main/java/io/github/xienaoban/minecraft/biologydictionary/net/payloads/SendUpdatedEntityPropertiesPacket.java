package io.github.xienaoban.minecraft.biologydictionary.net.payloads;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityProperty;
import io.github.xienaoban.minecraft.biologydictionary.common.net.Packet;
import io.github.xienaoban.minecraft.biologydictionary.common.net.PacketPayloadMeta;
import io.github.xienaoban.minecraft.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.minecraft.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.minecraft.biologydictionary.common.util.Misc;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;

public record SendUpdatedEntityPropertiesPacket(int entityId, CompoundTag vanillaNbt, CompoundTag extraNbt) implements Packet {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public CustomPacketPayload.Type<? extends Packet> type() { return META.type(); }

    @SuppressWarnings("unused")
    public SendUpdatedEntityPropertiesPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readNbt(), buf.readNbt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeNbt(vanillaNbt);
        buf.writeNbt(extraNbt);
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        Entity entity = ctx.player().level().getEntity(entityId);
        if (entity == null) {
            return;
        }

        // Save vanilla properties to the entity.
        if (vanillaNbt != null) {
            EntityUtils.mergeNbt(entity, vanillaNbt);
        }

        // Save extra properties to the entity.
        if (extraNbt != null) {
            for (EntityProperty<?> p : new EntityProperties<>(entity).getExtras()) {
                p.readFrom(extraNbt);
                p.saveTo(Misc.cast(entity));
            }
        }
    }
}
