package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public final class StringProperty extends AbstractProperty<String> {
    public StringProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag vanillaNbt) {
        if (vanillaNbt.contains(name(), Tag.TAG_STRING)) {
            set(vanillaNbt.getString(name()));
        } else {
            set(null);
        }
    }

    @Override
    public void writeTo(CompoundTag vanillaNbt) {
        if (get() != null) {
            vanillaNbt.putString(name(), get());
        } else {
            vanillaNbt.put(name(), new CompoundTag());
        }
    }
}
