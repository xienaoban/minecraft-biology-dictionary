package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;

public record SheepForceEatGrassSkill() implements EntityTargetedSkill<Sheep> {
    public static final Meta<SheepForceEatGrassSkill> META = new Meta<>() {
        @Override
        public SheepForceEatGrassSkill create(FriendlyByteBuf buf) {
            return new SheepForceEatGrassSkill();
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.empty();
        }

        @Override
        public String shortName() {
            return "force_eat_grass";
        }
    };

    public static boolean isGrassOrGrassBlock(Sheep entity) {
        Level level = EntityUtils.getLevel(entity);
        BlockPos blockPos = entity.blockPosition();
        return level.getBlockState(blockPos).is(BlockTags.EDIBLE_FOR_SHEEP)
                || level.getBlockState(blockPos.below()).is(Blocks.GRASS_BLOCK);
    }

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void serverAdditionalCheck(ServerContext<Sheep> ctx) {
        Permissions.checkMobHasGoalAndStart(ctx.entity(), EatBlockGoal.class);
    }

    @Override
    public void serverDo(ServerContext<Sheep> ctx) {
        ServerLevel level = (ServerLevel) EntityUtils.getLevel(ctx.entity());
        BlockPos blockPos = ctx.entity().blockPosition();

        boolean hasGrassBlock = level.getBlockState(blockPos.below()).is(Blocks.GRASS_BLOCK);
        boolean hasEdibleBlock = isGrassOrGrassBlock(ctx.entity());

        if (hasEdibleBlock || hasGrassBlock) {
            level.broadcastEntityEvent(ctx.entity(), (byte) 10);

            if (hasGrassBlock && level.getGameRules().get(GameRules.MOB_GRIEFING)) {
                BlockPos belowPos = blockPos.below();
                level.levelEvent(2001, belowPos, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
                level.setBlock(belowPos, Blocks.DIRT.defaultBlockState(), 2);
            } else if (!hasGrassBlock && level.getGameRules().get(GameRules.MOB_GRIEFING)) {
                level.destroyBlock(blockPos, false);
            }

            ctx.entity().ate();
        }
    }
}
