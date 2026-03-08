package io.github.xienaoban.biologydictionary.core.property.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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

    private final Class<T> clazz;
    private final Codec<T> codec;

    public CodecProperty(String propertyName, Class<?> clazz, Codec<T> codec) {
        super(propertyName);
        this.clazz = Misc.cast(clazz);
        this.codec = codec;
    }

    public Class<T> getClazz() { return clazz; }
    public Codec<T> getCodec() { return codec; }

    @Override
    public void readFrom(CompoundTag nbt) {
        Tag tag = nbt.get(name());
        if (tag == null || tag instanceof CompoundTag ct && ct.isEmpty()) {
            setVal(null);
        } else {
            // TODO: Need to find 1.21.1 alternative for nbt.read()
            // setVal(nbt.read(name(), codec).orElse(null));
            setVal(null);
        }
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
