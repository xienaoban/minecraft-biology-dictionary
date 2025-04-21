package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import net.minecraft.nbt.CompoundTag;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;

public class UnsupportedProperty extends AbstractProperty<Object> {
    public UnsupportedProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag vanillaNbt) {
        LOGGER.warn("Failed to read from NBT: not supported property: {}", name());
    }

    @Override
    public void writeTo(CompoundTag vanillaNbt) {
        LOGGER.warn("Failed to write to NBT: not supported property: {}", name());
    }
}
