package io.github.xienaoban.minecraft.biologydictionary.common.property;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public final class UuidProperty<E extends Entity> extends CodecProperty<E, UUID> {
    public UuidProperty(String propertyName) {
        super(propertyName, UUIDUtil.CODEC);
    }
}
