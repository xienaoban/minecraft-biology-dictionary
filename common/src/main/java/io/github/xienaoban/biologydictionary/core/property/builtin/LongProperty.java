package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class LongProperty<E extends Entity> extends AbstractProperty<E, Long> {
    public LongProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        setVal(nbt.getLong(name()).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            nbt.putLong(name(), getVal());
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}
