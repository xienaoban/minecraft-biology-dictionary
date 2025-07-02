package io.github.xienaoban.minecraft.biologydictionary.common.property;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.List;

public final class StringListProperty<E extends Entity> extends CodecProperty<E, List<String>> {
    public StringListProperty(String propertyName) {
        super(propertyName, Codec.STRING.listOf());
    }
}
