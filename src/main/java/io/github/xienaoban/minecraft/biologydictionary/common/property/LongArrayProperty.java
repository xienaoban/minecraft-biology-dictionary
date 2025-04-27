package io.github.xienaoban.minecraft.biologydictionary.common.property;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

public final class LongArrayProperty<E extends Entity> extends AbstractProperty<E, long[]> {
    public LongArrayProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (nbt.contains(name(), Tag.TAG_LONG_ARRAY)) {
            set(nbt.getLongArray(name()));
        } else {
            set(null);
        }
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (get() != null) {
            nbt.putLongArray(name(), get());
        } else {
            throw new IllegalPropertyStateException("array type must not be null");
        }
    }
}
