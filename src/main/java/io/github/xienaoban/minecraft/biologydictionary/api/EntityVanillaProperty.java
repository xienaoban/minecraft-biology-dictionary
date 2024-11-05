package io.github.xienaoban.minecraft.biologydictionary.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public interface EntityVanillaProperty<E extends Entity>  {
    void readFromNbt(CompoundTag vanillaNbt);
    void writeToNbt(CompoundTag vanillaNbt);
}
