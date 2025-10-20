package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.Skills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class MobSetNoAiSkill implements EntityTargetedSkill<Mob> {
    private static final int FRIENDLY_EXP_LVL_REQUIRED = 0;
    private static final int FRIENDLY_EXP_LVL_COST = 1;
    private static final int NEUTRAL_EXP_LVL_REQUIRED = 10;
    private static final int NEUTRAL_EXP_LVL_COST = 3;
    private static final int ENEMY_EXP_LVL_REQUIRED = 20;
    private static final int ENEMY_EXP_LVL_COST_MIN = 5;
    private static final float ENEMY_EXP_LVL_COST_FACTOR = 0.25F;

    public static int experienceLevelsRequired(Mob entity) {
        if (entity instanceof Enemy) {
            return ENEMY_EXP_LVL_REQUIRED;
        } else if (entity instanceof NeutralMob) {
            return NEUTRAL_EXP_LVL_REQUIRED;
        } else {
            return FRIENDLY_EXP_LVL_REQUIRED;
        }
    }

    public static int experienceLevelsCost(Mob entity) {
        if (entity instanceof Enemy) {
            return Math.max(ENEMY_EXP_LVL_COST_MIN, (int) (ENEMY_EXP_LVL_COST_FACTOR * entity.getMaxHealth()));
        } else if (entity instanceof NeutralMob) {
            return NEUTRAL_EXP_LVL_COST;
        } else {
            return FRIENDLY_EXP_LVL_COST;
        }
    }

    @Environment(EnvType.CLIENT)
    public static boolean activate(Mob entity, boolean noAi) {
        return Skills.sendEntityOrientedSkill(entity, noAi);
    }

    private static void check(Player player, Mob entity) {
        int lvlRequired = experienceLevelsRequired(entity);
        int lvlCost = experienceLevelsCost(entity);
        Permissions.checkPlayerCreativeOrExperienceLevel(player, Math.max(lvlRequired, lvlCost));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Tag clientSend(LocalPlayer player, Mob entity, Object... args) {
        boolean noAi = (boolean) args[0];
        check(player, entity);
        return ByteTag.valueOf(noAi);
    }

    @Override
    public void serverReceive(MinecraftServer server, ServerPlayer player, Mob entity, Tag args) {
        boolean noAi = args.asBoolean().orElseThrow();
        check(player, entity);
        CompoundTag tag = VanillaEntityProperties.OfMob.createNoAiProperty().withVal(noAi).toTag();

        // Clear the motion caused by collisions accumulated during the AI-disabled period
        // to prevent the entity from flying around randomly.
        VanillaEntityProperties.OfEntity.createMotionProperty().withVal(Vec3.ZERO).writeTo(tag);

        // Set the entity without AI to be invulnerable to avoid disrupting the balance of Survival Mode.
        if (!PlayerUtils.isCreative(player)) {
            VanillaEntityProperties.OfEntity.createInvulnerableProperty().withVal(noAi).writeTo(tag);
        }

        Skills.giveExperienceLevelsIfNotCreative(player, -experienceLevelsCost(entity));
        EntityUtils.mergeNbt(entity, tag);
    }
}
