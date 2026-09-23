package io.github.xienaoban.biologydictionary.core.property.builtin;

import io.github.xienaoban.biologydictionary.platform.util.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class DoubleProperty<E extends Entity> extends AbstractProperty<E, Double> {
    public DoubleProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        setVal(NbtUtils.getDoubleOr(nbt, name(), null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            NbtUtils.putDouble(nbt, name(), getVal());
        } else {
            NbtUtils.put(nbt, name(), new CompoundTag());
        }
    }
}
