package io.github.xienaoban.biologydictionary.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CustomData.class)
public interface CustomDataIMixin {
    @Accessor
    CompoundTag getTag();
}
