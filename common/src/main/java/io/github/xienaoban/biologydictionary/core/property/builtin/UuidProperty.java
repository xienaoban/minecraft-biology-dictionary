package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class UuidProperty<E extends Entity> extends AbstractProperty<E, UUID> {
    public UuidProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (nbt.contains(name())) {
            setVal(nbt.getUUID(name()));
        } else {
            setVal(null);
        }
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            nbt.putUUID(name(), getVal());
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}
