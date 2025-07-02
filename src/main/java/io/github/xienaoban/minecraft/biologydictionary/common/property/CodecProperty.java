package io.github.xienaoban.minecraft.biologydictionary.common.property;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class CodecProperty<E extends Entity, T> extends AbstractProperty<E, T> {
    private final Codec<T> codec;

    public CodecProperty(String propertyName, Codec<T> codec) {
        super(propertyName);
        this.codec = codec;
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        set(nbt.read(name(), codec).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        nbt.storeNullable(name(), codec, get());
    }
}
