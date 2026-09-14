package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class IntProperty<E extends Entity> extends AbstractProperty<E, Integer> {
    public IntProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        setVal(nbt.getInt(name()).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            nbt.putInt(name(), getVal());
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}
