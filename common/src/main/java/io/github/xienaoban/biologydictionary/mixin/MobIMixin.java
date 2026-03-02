package io.github.xienaoban.biologydictionary.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mob.class)
public interface MobIMixin {
    @Accessor("goalSelector")
    GoalSelector biologydictionary$getGoalSelector();
}
