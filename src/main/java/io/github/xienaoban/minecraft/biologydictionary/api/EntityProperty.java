package io.github.xienaoban.minecraft.biologydictionary.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public interface EntityProperty<E extends Entity>  {
    void readFromNbt(CompoundTag vanillaNbt, CompoundTag additionalNbt);
    void writeToNbt(CompoundTag vanillaNbt, CompoundTag additionalNbt);
}
