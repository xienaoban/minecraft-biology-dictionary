package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

public class BooleanProperty<E extends Entity> extends AbstractProperty<E, Boolean> {
    public BooleanProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (nbt.contains(name(), Tag.TAG_BYTE)) {
            setVal(nbt.getBoolean(name()));
        } else {
            setVal(null);
        }
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            nbt.putBoolean(name(), getVal());
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}
