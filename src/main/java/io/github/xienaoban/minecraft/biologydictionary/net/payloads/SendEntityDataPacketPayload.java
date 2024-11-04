package io.github.xienaoban.minecraft.biologydictionary.net.payloads;

import io.github.xienaoban.minecraft.biologydictionary.core.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.minecraft.biologydictionary.platform.net.PacketPayload;
import io.github.xienaoban.minecraft.biologydictionary.platform.net.PacketPayloadMeta;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionaryClient.BDC;

public record SendEntityDataPacketPayload(boolean notNull, int entityId, CompoundTag vanillaNbt, CompoundTag additionalNbt) implements PacketPayload {
    public static final PacketPayloadMeta<SendEntityDataPacketPayload> META = PacketPayloadMeta.create(SendEntityDataPacketPayload.class);

    @Override
    public @NotNull Type<? extends PacketPayload> type() { return META.type(); }

    @SuppressWarnings("unused")
    public SendEntityDataPacketPayload(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readInt(), buf.readNbt(), buf.readNbt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(notNull);
        buf.writeInt(entityId);
        buf.writeNbt(vanillaNbt);
        buf.writeNbt(additionalNbt);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        if (!notNull) return;

        Entity entity = BDC.getHitEntity();
        EntityProperties<?> properties = BDC.getHitEntityProperties();
        if (entity == null || entity.getId() != entityId || properties == null) return;
        properties.update(vanillaNbt, additionalNbt);
    }
}
