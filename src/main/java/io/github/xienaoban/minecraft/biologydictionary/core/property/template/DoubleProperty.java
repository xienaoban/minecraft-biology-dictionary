package io.github.xienaoban.minecraft.biologydictionary.core.property.template;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

@Environment(EnvType.CLIENT)
public final class DoubleProperty extends AbstractProperty<Double> {
    public DoubleProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag vanillaNbt) {
        if (vanillaNbt.contains(name(), Tag.TAG_DOUBLE)) {
            set(vanillaNbt.getDouble(name()));
        } else {
            set(null);
        }
    }

    @Override
    public void writeTo(CompoundTag vanillaNbt) {
        if (get() != null) {
            vanillaNbt.putDouble(name(), get());
        }
    }
}
