package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import static io.github.xienaoban.biologydictionary.BiologyDictionaryClient.BDC;

public record ReplyEntityDataPacket(boolean notNull, int entityId, CompoundTag vanillaNbt, CompoundTag extraNbt) implements Packet {
    public static final Packet.Factory<ReplyEntityDataPacket> FACTORY = ReplyEntityDataPacket::new;

    private ReplyEntityDataPacket(FriendlyByteBuf buf) {
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
        final class C { static void receive(ReplyEntityDataPacket packet, ClientNetApi.Context ctx) {
            if (!packet.notNull()) return;

            Entity entity = BDC.getHitEntity();
            EntityProperties<?> properties = BDC.getHitEntityProperties();
            if (entity == null || EntityUtils.getId(entity) != packet.entityId() || properties == null) return;
            properties.update(packet.vanillaNbt(), packet.extraNbt());
        }}
        C.receive(this, ctx);
    }
}
