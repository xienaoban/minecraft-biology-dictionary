package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySources;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.skill.general.HighlightEntitiesSkill;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

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
            buf.writeUtf(EntityUtils.getEntityTypeIdName(entityType));
        }
        buf.writeFloat(radius);
    }

    @ClientOnly
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        @ClientOnly final class CO { static void receive(
                ReplyHighlightEntitiesPacket packet, ClientNetApi.Context ctx) {
            if (!packet.allowed()) { return; }

            ClientWorldSession cws = ClientWorldSession.get();
            if (cws == null) {
                LOGGER.warn("Null ClientWorldSession. Ignored.", new RuntimeException());
                return;
            }

            ClientUtils.playScreenSound(SoundEvents.ENDER_DRAGON_FLAP, 0.6F, -10.0F);
            LocalPlayer player = ctx.player();
            int cnt = 0;
            Entity first = null;
            for (Entity e : ClientUtils.getClientLevel().entitiesForRendering()) {
                if (e.getType() != packet.entityType()) { continue; }
                if (!PlayerUtils.isWithinInteractionRange(player, e, packet.radius())) {
                    continue;
                }
                if (first == null) { first = e; }
                ++cnt;
                cws.getHighlightManager().highlightEntity(e, HighlightEntitiesSkill.TICKS);
            }
            if (first != null) {
                cws.getDiscoveryCacheManager().onDiscoveryEvent(DiscoverySources.HIGHLIGHT, player, first);
            }
            ClientUtils.sendCenteredMessage(TextUtils.translate(Lang.TEXT_HIGHLIGHTED_ENTITIES,
                    cnt, packet.entityType().getDescription(), packet.radius()));
        }}
        CO.receive(this, ctx);
    }
}
