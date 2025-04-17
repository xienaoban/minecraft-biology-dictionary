package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public final class LongArrayProperty extends AbstractProperty<long[]> {
    public LongArrayProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag vanillaNbt) {
        if (vanillaNbt.contains(name(), Tag.TAG_LONG_ARRAY)) {
            set(vanillaNbt.getLongArray(name()));
        } else {
            set(null);
        }
    }

    @Override
    public void writeTo(CompoundTag vanillaNbt) {
        if (get() != null) {
            vanillaNbt.putLongArray(name(), get());
        } else {
            throw new IllegalPropertyStateException("array type must not be null");
        }
    }
}
