package io.github.xienaoban.biologydictionary.mixin.loot;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(LootPool.class)
public interface LootPoolIMixin {
    @Accessor("entries")
    List<LootPoolEntryContainer> biologydictionary$getEntries();

    @Accessor("conditions")
    List<LootItemCondition> biologydictionary$getConditions();
}
