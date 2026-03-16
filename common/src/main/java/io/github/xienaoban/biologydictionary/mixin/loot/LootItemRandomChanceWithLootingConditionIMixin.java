package io.github.xienaoban.biologydictionary.mixin.loot;

import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootItemRandomChanceWithLootingCondition.class)
public interface LootItemRandomChanceWithLootingConditionIMixin {
    @Accessor("percent")
    float biologydictionary$getPercent();
}
