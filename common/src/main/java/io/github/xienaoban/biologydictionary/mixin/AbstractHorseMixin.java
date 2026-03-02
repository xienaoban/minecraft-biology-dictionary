package io.github.xienaoban.biologydictionary.mixin;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractHorse.class)
public interface AbstractHorseMixin {
    @Accessor("inventory")
    SimpleContainer biologydictionary$getInventory();
}
