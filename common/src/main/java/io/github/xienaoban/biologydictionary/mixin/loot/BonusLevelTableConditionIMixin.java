package io.github.xienaoban.biologydictionary.mixin.loot;

import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BonusLevelTableCondition.class)
public interface BonusLevelTableConditionIMixin {
    @Accessor("values")
    float[] biologydictionary$getValues();
}
