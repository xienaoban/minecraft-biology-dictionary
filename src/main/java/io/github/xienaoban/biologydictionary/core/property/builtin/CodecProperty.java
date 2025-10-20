package io.github.xienaoban.biologydictionary.core.property.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class CodecProperty<E extends Entity, T> extends AbstractProperty<E, T> {
    @SuppressWarnings("all")
    private static final Codec<?> EMPTY_CODEC = new Codec<>() {
        @Override
        public DataResult<?> encode(Object input, DynamicOps ops, Object prefix) {
            return DataResult.error(() -> "do nothing");
        }

        @Override
        public DataResult<?> decode(DynamicOps ops, Object input) {
            return DataResult.error(() -> "do nothing");
        }
    };

    @SuppressWarnings("all")
    public static <T> Codec<T> emptyCodec() {
        return (Codec<T>) EMPTY_CODEC;
    }

    private final Codec<T> codec;

    public CodecProperty(String propertyName, Codec<T> codec) {
        super(propertyName);
        this.codec = codec;
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        setVal(nbt.read(name(), codec).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        T v = getVal();
        if (v != null) {
            nbt.store(name(), codec, v);
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}
