package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public final class DoubleProperty<E extends Entity> extends AbstractProperty<E, Double> {
    public DoubleProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        setVal(nbt.getDouble(name()).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            nbt.putDouble(name(), getVal());
        } else {
            throw new IllegalPropertyStateException("primitive type must not be null");
        }
    }
}
