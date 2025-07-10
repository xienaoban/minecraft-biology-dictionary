package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public final class ByteProperty<E extends Entity> extends AbstractProperty<E, Byte> {
    public ByteProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        set(nbt.getByte(name()).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (get() != null) {
            nbt.putByte(name(), get());
        } else {
            throw new IllegalPropertyStateException("primitive type must not be null");
        }
    }
}
