package io.github.xienaoban.biologydictionary.mixin.loot;

import net.minecraft.core.HolderSet;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NestedLootTable.class)
public interface NestedLootTableIMixin {
    @Accessor("value")
    HolderSet<LootTable> biologydictionary$getValue();
}
