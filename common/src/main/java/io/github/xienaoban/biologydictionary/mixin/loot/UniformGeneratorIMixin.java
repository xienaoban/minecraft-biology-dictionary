package io.github.xienaoban.biologydictionary.mixin.loot;

import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(UniformGenerator.class)
public interface UniformGeneratorIMixin {
    @Accessor("min")
    NumberProvider biologydictionary$getMin();

    @Accessor("max")
    NumberProvider biologydictionary$getMax();
}
