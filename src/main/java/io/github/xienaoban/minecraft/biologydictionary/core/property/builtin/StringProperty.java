package io.github.xienaoban.minecraft.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public final class StringProperty<E extends Entity> extends AbstractProperty<E, String> {
    public StringProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        set(nbt.getString(name()).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (get() != null) {
            nbt.putString(name(), get());
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}
