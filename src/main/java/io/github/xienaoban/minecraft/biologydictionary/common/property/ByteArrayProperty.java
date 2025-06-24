package io.github.xienaoban.minecraft.biologydictionary.common.property;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public final class ByteArrayProperty<E extends Entity> extends AbstractProperty<E, byte[]> {
    public ByteArrayProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        set(nbt.getByteArray(name()).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (get() != null) {
            nbt.putByteArray(name(), get());
        } else {
            throw new IllegalPropertyStateException("array type must not be null");
        }
    }
}
