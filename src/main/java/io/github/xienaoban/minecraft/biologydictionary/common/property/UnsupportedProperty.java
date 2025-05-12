package io.github.xienaoban.minecraft.biologydictionary.common.property;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;

public class UnsupportedProperty<E extends Entity> extends AbstractProperty<E, Object> {
    public UnsupportedProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        LOGGER.debug("Failed to read from NBT: not supported property: {}", name());
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        LOGGER.debug("Failed to write to NBT: not supported property: {}", name());
    }
}
