package io.github.xienaoban.biologydictionary.core.skill.general;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public record HighlightEntitiesSkill(EntityType<?> entityType, float radius) implements GeneralSkill {
    public static final Meta<HighlightEntitiesSkill> META = new Meta<>() {
        @Override
        public HighlightEntitiesSkill create(FriendlyByteBuf buf) {
            return new HighlightEntitiesSkill(EntityUtils.getEntityType(buf.readUtf()), buf.readFloat());
        }

        @Override
        public SkillCost getDefaultCost() {
            return new SkillCost(17, 0, 0, List.of()); // 默认 17 经验点
        }

        @Override
        public Class<HighlightEntitiesSkill> getSkillClass() {
            return HighlightEntitiesSkill.class;
        }
    };

    public static final int TICKS = 12 * 20;
    public static final int NEAR_RADIUS = 20;
    public static final int NEAR_EXPERIENCE_POINTS_COST = 1;
    public static final int FAR_RADIUS = 100;
    public static final int FAR_EXPERIENCE_POINTS_COST = 16;
    public static final int BLINDNESS_TICKS = 40;
    public static final int BLOCK_TICKS = 6 * 20;

    private HighlightEntitiesSkill(FriendlyByteBuf buf) {
        this(EntityUtils.getEntityType(buf.readUtf()), buf.readFloat());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(entityType == null ? "" : EntityUtils.getEntityTypeIdString(entityType));
        buf.writeFloat(radius);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientAdditionalCheck(LocalPlayer player) {
        // 无额外检查
    }

    @Override
    public void serverAdditionalCheck(MinecraftServer server, ServerPlayer player) {
        if (entityType == null) {
            PlayerUtils.showClientCenteredMessage(player, TextUtils.translate(Lang.TEXT_FAILED_TO_HIGHLIGHT,
                    TextUtils.translate(Lang.TEXT_UNKNOWN_ENTITY_TYPE)));
        } else if (entityType == EntityType.PLAYER) {
            PlayerUtils.showClientCenteredMessage(player, TextUtils.translate(Lang.TEXT_FAILED_TO_HIGHLIGHT,
                    TextUtils.translate(Lang.TEXT_NOT_ALLOWED_TO_HIGHLIGHT_PLAYERS)));
        }
    }

    @Override
    public void serverDo(MinecraftServer server, ServerPlayer player) {
        boolean allowed = entityType != null && entityType != EntityType.PLAYER;
        if (PlayerUtils.isCreative(player) || PlayerUtils.isSpectator(player)) {
            allowed = true;
        }
        // 经验消耗现在由 SkillCost.serverConsume() 处理

        if (allowed) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLINDNESS_TICKS));
        }
        ServerNetManager.replyHighlightEntitiesSkill(player, allowed, entityType, radius);
    }
}
