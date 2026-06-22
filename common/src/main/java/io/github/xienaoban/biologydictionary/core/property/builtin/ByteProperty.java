package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class ByteProperty<E extends Entity> extends AbstractProperty<E, Byte> {
    public ByteProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        setVal(nbt.getByte(name()).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            nbt.putByte(name(), getVal());
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}
