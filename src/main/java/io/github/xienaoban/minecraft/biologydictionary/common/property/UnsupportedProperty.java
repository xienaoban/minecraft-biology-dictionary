package io.github.xienaoban.minecraft.biologydictionary.common.property;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class UnsupportedProperty<E extends Entity> extends AbstractProperty<E, Object> {
    public UnsupportedProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {}

    @Override
    public void writeTo(CompoundTag nbt) {}
}
