package io.github.xienaoban.biologydictionary.core.skill.general;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;
import io.github.xienaoban.biologydictionary.core.skill.PlayerSkills;
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

public record HighlightEntitiesSkill(EntityType<?> entityType, float radius) implements GeneralSkill {
    public static final GeneralSkill.Factory<HighlightEntitiesSkill> FACTORY = HighlightEntitiesSkill::new;
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
    public void clientCheck(LocalPlayer player) {}

    @Override
    public void serverCheck(MinecraftServer server, ServerPlayer player) {
        boolean allowed;
        if (entityType == null) {
            allowed = false;
            PlayerUtils.showClientCenteredMessage(player, TextUtils.translate(Lang.TEXT_FAILED_TO_HIGHLIGHT,
                    TextUtils.translate(Lang.TEXT_UNKNOWN_ENTITY_TYPE)));
        } else if (PlayerUtils.isCreative(player) || PlayerUtils.isSpectator(player)) {
            allowed = true;
        } else if (entityType == EntityType.PLAYER) {
            allowed = false;
            PlayerUtils.showClientCenteredMessage(player, TextUtils.translate(Lang.TEXT_FAILED_TO_HIGHLIGHT,
                    TextUtils.translate(Lang.TEXT_NOT_ALLOWED_TO_HIGHLIGHT_PLAYERS)));
        } else {
            int experience;
            if (radius <= NEAR_RADIUS) {
                experience = NEAR_EXPERIENCE_POINTS_COST;
            } else if (radius <= FAR_RADIUS) {
                experience = FAR_EXPERIENCE_POINTS_COST;
            } else {
                experience = Integer.MAX_VALUE;
            }
            if (PlayerUtils.getExperiencePoints(player) < experience) {
                allowed = false;
                PlayerUtils.showClientCenteredMessage(player, TextUtils.translate(Lang.TEXT_FAILED_TO_HIGHLIGHT,
                        TextUtils.translate(Lang.TEXT_NOT_ENOUGH_EXPERIENCE_LEVELS, experience)));
            } else {
                allowed = true;
                PlayerSkills.giveExperiencePointsIfNotCreative(player, -experience);
            }
        }

        if (allowed) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLINDNESS_TICKS));
        }
        ServerNetManager.replyHighlightEntitiesSkill(player, allowed, entityType, radius);
    }
}
