package io.github.xienaoban.biologydictionary.mixin;

import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TemptGoal.class)
public interface TemptGoalIMixin {
    @Accessor("items")
    Ingredient biologydictionary$getItems();
}
