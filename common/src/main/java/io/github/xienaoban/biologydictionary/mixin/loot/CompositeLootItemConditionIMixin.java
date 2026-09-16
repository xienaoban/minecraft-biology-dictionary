package io.github.xienaoban.biologydictionary.mixin.loot;

import net.minecraft.core.HolderSet;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CompositeLootItemCondition.class)
public interface CompositeLootItemConditionIMixin {
    @Accessor("terms")
    HolderSet<LootItemCondition> biologydictionary$getTerms();
}
