package io.github.xienaoban.minecraft.biologydictionary.common.property;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public final class UuidProperty<E extends Entity> extends AbstractProperty<E, UUID> {
    public UuidProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (nbt.contains(name(), Tag.TAG_INT_ARRAY)) {
            set(nbt.getUUID(name()));
        } else {
            set(null);
        }
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (get() != null) {
            nbt.putUUID(name(), get());
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}
