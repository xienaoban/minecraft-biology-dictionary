package io.github.xienaoban.minecraft.biologydictionary.common.property;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.List;

public final class StringListProperty<E extends Entity> extends AbstractProperty<E, List<String>> {
    public StringListProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        set(nbt.read(name(), Codec.STRING.listOf()).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        nbt.storeNullable(name(), Codec.STRING.listOf(), get());
    }
}
