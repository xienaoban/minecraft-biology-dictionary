package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.PlayerSkills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class SheepForceEatGrassSkill implements EntityTargetedSkill<Sheep> {
    public static final int EXP_COST = 4;

    @Environment(EnvType.CLIENT)
    public static boolean activate(Sheep entity) {
        return PlayerSkills.sendEntityTargetedSkill(entity);
    }

    /**
     * @see net.minecraft.world.entity.ai.goal.EatBlockGoal#tick()
     */
    public static boolean isGrassOrGrassBlock(Sheep entity) {
        Level level = entity.level();
        BlockPos blockPos = entity.blockPosition();
        return level.getBlockState(blockPos).is(BlockTags.EDIBLE_FOR_SHEEP)
                || level.getBlockState(blockPos.below()).is(Blocks.GRASS_BLOCK);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Tag clientSend(LocalPlayer player, Sheep entity, Object... args) {
        Permissions.checkPlayerCreativeOrExperiencePoints(player, EXP_COST);
        return ByteTag.valueOf(false);
    }

    @Override
    public void serverReceive(MinecraftServer server, ServerPlayer player, Sheep entity, Tag args) {
        Permissions.checkLegalArg(args.asBoolean().orElseThrow(), false);
        Permissions.checkPlayerCreativeOrExperiencePoints(player, EXP_COST);
        PlayerSkills.giveExperiencePointsIfNotCreative(player, -EXP_COST);
        Permissions.checkMobHasGoalAndStart(entity, EatBlockGoal.class);
    }
}
