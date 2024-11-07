package io.github.xienaoban.minecraft.biologydictionary.net.payloads;

import io.github.xienaoban.minecraft.biologydictionary.util.EntityUtils;
import io.github.xienaoban.minecraft.biologydictionary.platform.net.PacketPayload;
import io.github.xienaoban.minecraft.biologydictionary.platform.net.PacketPayloadMeta;
import io.github.xienaoban.minecraft.biologydictionary.platform.net.ServerNetApi;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record RequestEntityDataPacketPayload(int entityId) implements PacketPayload {
    public static final PacketPayloadMeta<RequestEntityDataPacketPayload> META = PacketPayloadMeta.create(RequestEntityDataPacketPayload.class);

    @Override
    public @NotNull Type<? extends PacketPayload> type() { return META.type(); }

    @SuppressWarnings("unused")
    public RequestEntityDataPacketPayload(FriendlyByteBuf buf) { this(buf.readInt()); }

    @Override
    public void write(FriendlyByteBuf buf) { buf.writeInt(entityId); }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        Entity entity = ctx.player().getCommandSenderWorld().getEntity(entityId);

        SendEntityDataPacketPayload toSend;
        if (entity != null) {
            // Write vanilla NBT data.
            CompoundTag vanillaNbt = entity.saveWithoutId(new CompoundTag());

            // Write data that not in vanilla NBT.
            CompoundTag extraNbt = new CompoundTag();
            for (var clazz : EntityUtils.topDown(entity)) {
                // for (var registry : EntityPropertyWidgetManager.getInstance().getRegistries(clazz)) {
                //     for (var handler : registry.getEntityDataBufHandlers().values()) {
                //         handler.write(extraNbt, Misc.cast(entity));
                //     }
                // }
            }
            toSend = new SendEntityDataPacketPayload(true, entity.getId(), vanillaNbt, extraNbt);
        } else {
            toSend = new SendEntityDataPacketPayload(false, -1, null, null);
        }

        ServerNetApi.send(ctx.player(), toSend);
    }
}
