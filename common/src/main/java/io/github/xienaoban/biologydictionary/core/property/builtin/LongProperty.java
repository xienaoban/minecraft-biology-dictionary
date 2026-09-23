package io.github.xienaoban.biologydictionary.core.property.builtin;

import io.github.xienaoban.biologydictionary.platform.util.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class LongProperty<E extends Entity> extends AbstractProperty<E, Long> {
    public LongProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        setVal(NbtUtils.getLongOr(nbt, name(), null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            NbtUtils.putLong(nbt, name(), getVal());
        } else {
            NbtUtils.put(nbt, name(), new CompoundTag());
        }
    }
}
