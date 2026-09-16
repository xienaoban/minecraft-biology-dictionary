package io.github.xienaoban.biologydictionary.mixin.loot;

import net.minecraft.core.HolderSet;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SequenceFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SequenceFunction.class)
public interface SequenceFunctionIMixin {
    @Accessor("functions")
    HolderSet<LootItemFunction> biologydictionary$getFunctions();
}
