package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class ShortProperty<E extends Entity> extends AbstractProperty<E, Short> {
    public ShortProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        setVal(nbt.getShort(name()).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            nbt.putShort(name(), getVal());
        } else {
            throw new IllegalPropertyStateException("primitive type must not be null");
        }
    }
}
