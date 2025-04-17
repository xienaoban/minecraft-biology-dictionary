package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.UUID;

public final class UuidProperty extends AbstractProperty<UUID> {
    public UuidProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag vanillaNbt) {
        if (vanillaNbt.contains(name(), Tag.TAG_INT_ARRAY)) {
            set(vanillaNbt.getUUID(name()));
        } else {
            set(null);
        }
    }

    @Override
    public void writeTo(CompoundTag vanillaNbt) {
        if (get() != null) {
            vanillaNbt.putUUID(name(), get());
        } else {
            vanillaNbt.put(name(), new CompoundTag());
        }
    }
}
