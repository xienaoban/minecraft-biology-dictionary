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
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;

public class HighlightEntitiesSkill implements GeneralSkill {
    public static final int TICKS = 12 * 20;
    public static final int NEAR_RADIUS = 20;
    public static final int NEAR_EXPERIENCE_POINTS_COST = 1;
    public static final int FAR_RADIUS = 100;
    public static final int FAR_EXPERIENCE_POINTS_COST = 16;
    public static final int BLINDNESS_TICKS = 40;

    public static final int BLOCK_TICKS = 6 * 20;

    @Environment(EnvType.CLIENT)
    public static boolean activate(EntityType<?> entityType, float radius) {
        return PlayerSkills.sendCommonSkill(entityType, radius);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Tag clientSend(LocalPlayer player, Object... args) {
        EntityType<?> entityType = (EntityType<?>) args[0];
        float radius = (float) args[1];
        ListTag res = new ListTag();
        if (entityType == null) {
            res.add(StringTag.valueOf(""));
        } else {
            res.add(StringTag.valueOf(EntityUtils.getEntityTypeIdString(entityType)));
        }
        res.add(FloatTag.valueOf(radius));
        return res;
    }

    @Override
    public void serverReceive(MinecraftServer server, ServerPlayer player, Tag args) {
        ListTag argList = args.asList().orElseThrow();
        String entityTypeId = argList.getString(0).orElseThrow();
        EntityType<?> entityType = EntityUtils.getEntityType(entityTypeId);
        float radius = argList.getFloat(1).orElseThrow();

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
