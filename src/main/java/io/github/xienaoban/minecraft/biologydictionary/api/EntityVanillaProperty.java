package io.github.xienaoban.minecraft.biologydictionary.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;

@Environment(EnvType.CLIENT)
public interface EntityVanillaProperty {
    String name();

    void readFrom(CompoundTag vanillaNbt);
    void writeTo(CompoundTag vanillaNbt);
}
