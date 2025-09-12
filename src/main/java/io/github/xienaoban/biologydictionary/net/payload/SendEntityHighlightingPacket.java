package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.Const;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.client.HighlightManager;
import io.github.xienaoban.biologydictionary.common.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketPayloadMeta;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record SendEntityHighlightingPacket(boolean allowed, EntityType<?> entityType, float radius) implements Packet {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public Type<? extends Packet> type() { return META.type(); }

    @SuppressWarnings("unused")
    public SendEntityHighlightingPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean(), EntityUtils.getEntityType(buf.readUtf()), buf.readFloat());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(allowed);
        if (entityType == null) {
            buf.writeUtf("");
        } else {
            buf.writeUtf(EntityUtils.getEntityTypeIdString(entityType));
        }
        buf.writeFloat(radius);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        if (!allowed) { return; }

        McClientUtils.playScreenSound(SoundEvents.ENDER_DRAGON_FLAP, 0.6F, -10.0F);
        LocalPlayer player = ctx.player();
        int cnt = 0;
        for (Entity e : McClientUtils.getClientLevel().entitiesForRendering()) {
            if (e.getType() != entityType) { continue; }
            if (player.distanceToSqr(e) > radius * radius) {
                continue;
            }
            ++cnt;
            HighlightManager.highlightEntity(e, Const.HIGHLIGHT_ENTITIES_TICKS);
        }
        McClientUtils.sendCenteredMessage(Component.translatable(Lang.TEXT_HIGHLIGHTED_ENTITIES,
                cnt, entityType.getDescription(), radius));
    }
}
