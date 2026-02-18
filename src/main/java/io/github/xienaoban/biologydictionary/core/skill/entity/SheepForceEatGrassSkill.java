package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
            return SkillCost.empty(); // 无消耗
        }

        @Override
        public Class<SheepForceEatGrassSkill> getSkillClass() {
            return SheepForceEatGrassSkill.class;
        }
    };

    /**
     * @see net.minecraft.world.entity.ai.goal.EatBlockGoal#tick()
     */
    public static boolean isGrassOrGrassBlock(Sheep entity) {
        Level level = EntityUtils.getLevel(entity);
        BlockPos blockPos = entity.blockPosition();
        return level.getBlockState(blockPos).is(BlockTags.EDIBLE_FOR_SHEEP)
                || level.getBlockState(blockPos.below()).is(Blocks.GRASS_BLOCK);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        // No data to write
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientAdditionalCheck(LocalPlayer player, Sheep entity) {
        // 无额外检查
    }

    @Override
    public void serverAdditionalCheck(MinecraftServer server, ServerPlayer player, Sheep entity) {
        Permissions.checkMobHasGoalAndStart(entity, EatBlockGoal.class);
    }

    @Override
    public void serverDo(MinecraftServer server, ServerPlayer player, Sheep sheep) {
        // 在 Minecraft 1.21.11 中，调用 ate() 方法来触发吃草后的效果（如掉落羊毛）
        // 参考 EatBlockGoal#tick() 中的实现
        ServerLevel level = (ServerLevel) EntityUtils.getLevel(sheep);
        BlockPos blockPos = sheep.blockPosition();

        // 检查脚下是否有草方块或可食用的草
        boolean hasGrassBlock = level.getBlockState(blockPos.below()).is(Blocks.GRASS_BLOCK);
        boolean hasEdibleBlock = isGrassOrGrassBlock(sheep);

        if (hasEdibleBlock || hasGrassBlock) {
            // 触发吃草动画
            level.broadcastEntityEvent(sheep, (byte) 10);

            // 如果是草方块，破坏它
            if (hasGrassBlock && level.getGameRules().get(GameRules.MOB_GRIEFING)) {
                BlockPos belowPos = blockPos.below();
                level.levelEvent(2001, belowPos, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
                level.setBlock(belowPos, Blocks.DIRT.defaultBlockState(), 2);
            } else if (!hasGrassBlock && level.getGameRules().get(GameRules.MOB_GRIEFING)) {
                // 如果是可食用的草，破坏它
                level.destroyBlock(blockPos, false);
            }

            // 调用 ate() 方法触发吃草后的效果（如再生羊毛）
            sheep.ate();
        }
    }
}
