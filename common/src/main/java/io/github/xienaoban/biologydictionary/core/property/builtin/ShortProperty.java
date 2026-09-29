package io.github.xienaoban.biologydictionary.core.property.builtin;

import io.github.xienaoban.biologydictionary.platform.util.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class ShortProperty<E extends Entity> extends AbstractProperty<E, Short> {
    public ShortProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        setVal(NbtUtils.getShortOr(nbt, name(), null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            NbtUtils.putShort(nbt, name(), getVal());
        } else {
            NbtUtils.put(nbt, name(), new CompoundTag());
        }
    }
}
