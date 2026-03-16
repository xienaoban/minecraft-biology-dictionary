package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

public class LongArrayProperty<E extends Entity> extends AbstractProperty<E, long[]> {
    public LongArrayProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (nbt.contains(name(), Tag.TAG_LONG_ARRAY)) {
            setVal(nbt.getLongArray(name()));
        } else {
            setVal(null);
        }
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            nbt.putLongArray(name(), getVal());
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}
