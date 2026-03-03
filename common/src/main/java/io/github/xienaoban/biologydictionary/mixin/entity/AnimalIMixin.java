package io.github.xienaoban.biologydictionary.mixin.entity;

import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Animal.class)
public interface AnimalIMixin {
    @Accessor("PARENT_AGE_AFTER_BREEDING")
    static int biologydictionary$getParentAgeAfterBreeding() { throw new AssertionError(); }
}
