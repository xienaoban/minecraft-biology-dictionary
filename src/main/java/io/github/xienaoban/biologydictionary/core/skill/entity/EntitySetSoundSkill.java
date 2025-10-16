package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.Skills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;

public class EntitySetSoundSkill implements EntityTargetedSkill<Entity> {
    private static final int FRIENDLY_EXP_PT_COST = 4;
    private static final int NEUTRAL_EXP_PT_COST = 16;
    private static final int ENEMY_EXP_PT_COST = 64;

    public static int experiencePointsCost(Entity entity) {
        if (entity instanceof Enemy) {
            return ENEMY_EXP_PT_COST;
        } else if (entity instanceof NeutralMob) {
            return NEUTRAL_EXP_PT_COST;
        } else {
            return FRIENDLY_EXP_PT_COST;
        }
    }

    @Environment(EnvType.CLIENT)
    public static boolean activate(Entity entity, boolean silent) {
        return Skills.sendEntityOrientedSkill(entity, silent);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Tag clientSend(LocalPlayer player, Entity entity, Object... args) {
        boolean silent = (boolean) args[0];
        Permissions.checkTargetPlayerLowerGameMode(player, entity);
        Permissions.checkPlayerCreativeOrExperiencePoints(player, experiencePointsCost(entity));
        return ByteTag.valueOf(silent);
    }

    @Override
    public void serverReceive(MinecraftServer server, ServerPlayer player, Entity entity, Tag args) {
        boolean silent = args.asBoolean().orElseThrow();
        Permissions.checkTargetPlayerLowerGameMode(player, entity);
        Permissions.checkPlayerCreativeOrExperiencePoints(player, experiencePointsCost(entity));
        Skills.giveExperiencePointsIfNotCreative(player, -experiencePointsCost(entity));
        EntityUtils.mergeNbt(entity, VanillaEntityProperties.OfEntity.createSilentProperty().toNbtWith(silent));
    }
}
