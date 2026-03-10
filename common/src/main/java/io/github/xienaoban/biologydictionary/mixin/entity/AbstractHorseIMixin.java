package io.github.xienaoban.biologydictionary.mixin.entity;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractHorse.class)
public interface AbstractHorseIMixin {
    @Accessor("inventory")
    SimpleContainer biologydictionary$getInventory();
}
