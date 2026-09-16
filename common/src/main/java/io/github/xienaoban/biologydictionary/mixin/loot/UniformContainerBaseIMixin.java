package io.github.xienaoban.biologydictionary.mixin.loot;

import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(UniformContainerBase.class)
public interface UniformContainerBaseIMixin {
    @Accessor("weight")
    int biologydictionary$getWeight();
}
