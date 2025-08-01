package io.github.xienaoban.biologydictionary.net.payloads;

import io.github.xienaoban.biologydictionary.Const;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketPayloadMeta;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.McUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;

public record RequestEntityHighlightingPacket(EntityType<?> entityType, float radius) implements Packet {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public Type<? extends Packet> type() { return META.type(); }

    @SuppressWarnings("unused")
    public RequestEntityHighlightingPacket(FriendlyByteBuf buf) {
        this(EntityUtils.getEntityType(buf.readUtf()), buf.readFloat());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        if (entityType == null) {
            buf.writeUtf("");
        } else {
            buf.writeUtf(EntityUtils.getEntityTypeIdString(entityType));
        }
        buf.writeFloat(radius);
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        ServerPlayer player = ctx.player();

        boolean allowed;
        if (entityType == null) {
            allowed = false;
            McUtils.showClientCenteredMessage(player, Component.translatable(Lang.TEXT_FAILED_TO_HIGHLIGHT,
                    Component.translatable(Lang.TEXT_UNKNOWN_ENTITY_TYPE)));
        } else if (player.gameMode() == GameType.CREATIVE || player.gameMode() == GameType.SPECTATOR) {
            allowed = true;
        } else if (entityType == EntityType.PLAYER) {
            allowed = false;
            McUtils.showClientCenteredMessage(player, Component.translatable(Lang.TEXT_FAILED_TO_HIGHLIGHT,
                    Component.translatable(Lang.TEXT_NOT_ALLOWED_TO_HIGHLIGHT_PLAYERS)));
        } else if (player.totalExperience < Const.HIGHLIGHT_ENTITIES_EXP) {
            allowed = false;
            McUtils.showClientCenteredMessage(player, Component.translatable(Lang.TEXT_FAILED_TO_HIGHLIGHT,
                    Component.translatable(Lang.TEXT_NOT_ENOUGH_EXPERIENCE)));
        } else {
            allowed = true;
            player.giveExperiencePoints(-Const.HIGHLIGHT_ENTITIES_EXP);
        }

        if (allowed) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, Const.HIGHLIGHT_ENTITIES_BLINDNESS_TICKS));
        }
        ServerNetApi.send(player, new SendEntityHighlightingPacket(allowed, entityType, radius));
    }
}
