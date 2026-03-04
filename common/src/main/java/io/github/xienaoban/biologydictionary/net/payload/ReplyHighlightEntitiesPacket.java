package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.client.HighlightManager;
import io.github.xienaoban.biologydictionary.core.skill.general.HighlightEntitiesSkill;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record ReplyHighlightEntitiesPacket(boolean allowed, EntityType<?> entityType, float radius) implements Packet {
    public static final Packet.Factory<ReplyHighlightEntitiesPacket> FACTORY = ReplyHighlightEntitiesPacket::new;

    private ReplyHighlightEntitiesPacket(FriendlyByteBuf buf) {
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
        final class C { static void receive(ReplyHighlightEntitiesPacket packet, ClientNetApi.Context ctx) {
            if (!packet.allowed()) { return; }

            ClientUtils.playScreenSound(SoundEvents.ENDER_DRAGON_FLAP, 0.6F, -10.0F);
            LocalPlayer player = ctx.player();
            int cnt = 0;
            for (Entity e : ClientUtils.getClientLevel().entitiesForRendering()) {
                if (e.getType() != packet.entityType()) { continue; }
                if (player.distanceToSqr(e) > packet.radius() * packet.radius()) {
                    continue;
                }
                ++cnt;
                HighlightManager.highlightEntity(e, HighlightEntitiesSkill.TICKS);
            }
            ClientUtils.sendCenteredMessage(TextUtils.translate(Lang.TEXT_HIGHLIGHTED_ENTITIES,
                    cnt, packet.entityType().getDescription(), packet.radius()));
        }}
        C.receive(this, ctx);
    }
}
