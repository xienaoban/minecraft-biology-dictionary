package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class ByteProperty<E extends Entity> extends AbstractProperty<E, Byte> {
    public ByteProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (nbt.contains(name())) {
            setVal(nbt.getByte(name()));
        } else {
            setVal(null);
        }
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            nbt.putByte(name(), getVal());
        } else {
            throw new IllegalPropertyStateException("primitive type must not be null");
        }
    }
}
