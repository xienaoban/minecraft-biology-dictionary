package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public final class ByteArrayProperty<E extends Entity> extends AbstractProperty<E, byte[]> {
    public ByteArrayProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        setVal(nbt.getByteArray(name()).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            nbt.putByteArray(name(), getVal());
        } else {
            throw new IllegalPropertyStateException("array type must not be null");
        }
    }
}
