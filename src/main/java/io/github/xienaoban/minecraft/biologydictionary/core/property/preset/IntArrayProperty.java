package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

@Environment(EnvType.CLIENT)
public final class IntArrayProperty extends AbstractProperty<int[]> {
    public IntArrayProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag vanillaNbt) {
        if (vanillaNbt.contains(name(), Tag.TAG_INT_ARRAY)) {
            set(vanillaNbt.getIntArray(name()));
        } else {
            set(null);
        }
    }

    @Override
    public void writeTo(CompoundTag vanillaNbt) {
        if (get() != null) {
            vanillaNbt.putIntArray(name(), get());
        } else {
            vanillaNbt.remove(name());
        }
    }
}
