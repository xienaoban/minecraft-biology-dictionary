package io.github.xienaoban.biologydictionary.mixin;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(EatBlockGoal.class)
public interface EatBlockGoalIMixin {
    @Accessor("IS_TALL_GRASS")
    static Predicate<BlockState> biologydictionary$getIsTailGrass() {
        throw new AssertionError();
    }
}
