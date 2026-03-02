package io.github.xienaoban.biologydictionary.mixin;

import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(LootPoolSingletonContainer.class)
public interface LootPoolSingletonContainerIMixin {
    @Accessor("weight")
    int biologydictionary$getWeight();

    @Accessor("functions")
    List<LootItemFunction> biologydictionary$getFunctions();
}
