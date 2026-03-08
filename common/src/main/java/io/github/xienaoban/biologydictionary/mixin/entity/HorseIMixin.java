package io.github.xienaoban.biologydictionary.mixin.entity;

import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Variant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Horse.class)
public interface HorseIMixin {
    @Invoker("setVariantAndMarkings")
    void biologydictionary$invokeSetVariantAndMarkings(Variant variant, Markings markings);
}
