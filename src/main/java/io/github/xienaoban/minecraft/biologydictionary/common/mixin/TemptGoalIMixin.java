package io.github.xienaoban.minecraft.biologydictionary.common.mixin;

import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(TemptGoal.class)
public interface TemptGoalIMixin {
    @Accessor
    Predicate<ItemStack> getItems();
}
