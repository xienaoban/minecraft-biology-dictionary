package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

public class IntArrayProperty<E extends Entity> extends AbstractProperty<E, int[]> {
    public IntArrayProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (nbt.contains(name(), Tag.TAG_INT_ARRAY)) {
            setVal(nbt.getIntArray(name()));
        } else {
            setVal(null);
        }
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            nbt.putIntArray(name(), getVal());
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}
