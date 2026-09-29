package io.github.xienaoban.biologydictionary.core.property.builtin;

import io.github.xienaoban.biologydictionary.platform.util.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class ByteProperty<E extends Entity> extends AbstractProperty<E, Byte> {
    public ByteProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        setVal(NbtUtils.getByteOr(nbt, name(), null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            NbtUtils.putByte(nbt, name(), getVal());
        } else {
            NbtUtils.put(nbt, name(), new CompoundTag());
        }
    }
}
