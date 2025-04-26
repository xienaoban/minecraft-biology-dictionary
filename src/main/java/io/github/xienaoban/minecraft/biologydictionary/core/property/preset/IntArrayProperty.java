package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

public final class IntArrayProperty<E extends Entity> extends AbstractProperty<E, int[]> {
    public IntArrayProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (nbt.contains(name(), Tag.TAG_INT_ARRAY)) {
            set(nbt.getIntArray(name()));
        } else {
            set(null);
        }
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (get() != null) {
            nbt.putIntArray(name(), get());
        } else {
            throw new IllegalPropertyStateException("array type must not be null");
        }
    }
}
