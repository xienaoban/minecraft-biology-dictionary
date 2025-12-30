package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class FloatProperty<E extends Entity> extends AbstractProperty<E, Float> {
    public FloatProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        setVal(nbt.getFloat(name()).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            nbt.putFloat(name(), getVal());
        } else {
            throw new IllegalPropertyStateException("primitive type must not be null");
        }
    }
}
