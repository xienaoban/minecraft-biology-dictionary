package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import net.minecraft.nbt.CompoundTag;

public class UnsupportedProperty extends AbstractProperty<Object> {
    public UnsupportedProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag vanillaNbt) {
        throw new IllegalPropertyStateException("unsupported");
    }

    @Override
    public void writeTo(CompoundTag vanillaNbt) {
        throw new IllegalPropertyStateException("unsupported");
    }
}
