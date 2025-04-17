package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public final class IntProperty extends AbstractProperty<Integer> {
    public IntProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag vanillaNbt) {
        if (vanillaNbt.contains(name(), Tag.TAG_INT)) {
            set(vanillaNbt.getInt(name()));
        } else {
            set(null);
        }
    }

    @Override
    public void writeTo(CompoundTag vanillaNbt) {
        if (get() != null) {
            vanillaNbt.putInt(name(), get());
        } else {
            throw new IllegalPropertyStateException("primitive type must not be null");
        }
    }
}
