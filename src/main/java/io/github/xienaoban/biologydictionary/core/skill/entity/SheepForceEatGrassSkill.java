package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.PlayerSkills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public record SheepForceEatGrassSkill() implements EntityTargetedSkill<Sheep> {
    public static final Factory<SheepForceEatGrassSkill> FACTORY = SheepForceEatGrassSkill::new;

    public static final int EXP_PT_COST = 4;

    /**
     * @see net.minecraft.world.entity.ai.goal.EatBlockGoal#tick()
     */
    public static boolean isGrassOrGrassBlock(Sheep entity) {
        Level level = EntityUtils.getLevel(entity);
        BlockPos blockPos = entity.blockPosition();
        return level.getBlockState(blockPos).is(BlockTags.EDIBLE_FOR_SHEEP)
                || level.getBlockState(blockPos.below()).is(Blocks.GRASS_BLOCK);
    }

    private SheepForceEatGrassSkill(FriendlyByteBuf buf) {
        this();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        // No data to write
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientCheck(LocalPlayer player, Sheep entity) {
        Permissions.checkPlayerCreativeOrExperiencePoints(player, EXP_PT_COST);
    }

    @Override
    public void serverCheck(MinecraftServer server, ServerPlayer player, Sheep entity) {
        Permissions.checkPlayerCreativeOrExperiencePoints(player, EXP_PT_COST);
        PlayerSkills.giveExperiencePointsIfNotCreative(player, -EXP_PT_COST);
        Permissions.checkMobHasGoalAndStart(entity, EatBlockGoal.class);
    }
}
