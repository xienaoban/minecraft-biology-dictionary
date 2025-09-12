package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.api.EntityProperty;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketPayloadMeta;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.common.util.Pair;
import io.github.xienaoban.biologydictionary.core.handler.Permissions;
import io.github.xienaoban.biologydictionary.core.handler.PropertyUpdaters;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public record SendUpdatedEntityPropertiesPacket(int entityId, ResourceLocation key, Tag args) implements Packet {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public CustomPacketPayload.Type<? extends Packet> type() { return META.type(); }

    @SuppressWarnings("unused")
    public SendUpdatedEntityPropertiesPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readResourceLocation(), buf.readNbt(NbtAccounter.unlimitedHeap()));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeResourceLocation(key);
        buf.writeNbt(args);
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        Entity entity = ctx.player().level().getEntity(entityId);
        if (entity == null) {
            LOGGER.warn("Entity ID not found: {}", entityId);
            BiologyDictionary.sendCenteredWarning(ctx.player(), Component.translatable(Lang.TEXT_UNKNOWN_ENTITY_ID));
            return;
        }

        CompoundTag vanillaNbt;
        CompoundTag extraNbt;
        try {
            PropertyUpdaters.Handler handler = PropertyUpdaters.getHandler(key);
            Pair<CompoundTag, CompoundTag> nbts = handler.serverReceive(args, ctx);
            vanillaNbt = nbts.first();
            extraNbt = nbts.second();
        } catch (Permissions.NoPermissionException e) {
            LOGGER.warn(Misc.getStackToString(e));
            BiologyDictionary.sendCenteredWarning(ctx.player(), e.getGameMessage());
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
                p.setTo(Misc.cast(entity));
            }
        }
    }

    public static SendUpdatedEntityPropertiesPacket of(int entityId, ResourceLocation key, Object... args) {
        try {
            PropertyUpdaters.Handler handler = PropertyUpdaters.getHandler(key);
            Tag tagArgs = handler.clientSend(args);
            return new SendUpdatedEntityPropertiesPacket(entityId, key, tagArgs);
        } catch (Permissions.NoPermissionException e) {
            BiologyDictionaryClient.sendCenteredWarning(e.getGameMessage());
        }
        return null;
    }
}
