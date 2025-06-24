package io.github.xienaoban.minecraft.biologydictionary.common.property;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public final class UuidProperty<E extends Entity> extends AbstractProperty<E, UUID> {
    public UuidProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        set(nbt.read(name(), UUIDUtil.CODEC).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        nbt.storeNullable(name(), UUIDUtil.CODEC, get());
    }
}
