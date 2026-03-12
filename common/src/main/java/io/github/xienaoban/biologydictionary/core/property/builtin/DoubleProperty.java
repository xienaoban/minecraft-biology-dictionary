package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

public class DoubleProperty<E extends Entity> extends AbstractProperty<E, Double> {
    public DoubleProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (nbt.contains(name(), Tag.TAG_DOUBLE)) {
            setVal(nbt.getDouble(name()));
        } else {
            setVal(null);
        }
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            nbt.putDouble(name(), getVal());
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}
