package io.github.xienaoban.minecraft.biologydictionary.api;

import net.minecraft.nbt.CompoundTag;

public interface EntityProperty {
    String name();

    void readFrom(CompoundTag vanillaNbt);
    void writeTo(CompoundTag vanillaNbt);
}
