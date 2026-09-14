package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class ByteArrayProperty<E extends Entity> extends AbstractProperty<E, byte[]> {
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
            nbt.put(name(), new CompoundTag());
        }
    }
}
