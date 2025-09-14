package io.github.xienaoban.biologydictionary.core.property.builtin;

import io.github.xienaoban.biologydictionary.core.property.EntityProperty;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public abstract class AbstractProperty<E extends Entity, T> implements EntityProperty<E> {
    private final String propertyName;
    private volatile T value;

    public AbstractProperty(String propertyName) {
        this.propertyName = propertyName;
        this.value = null;
    }

    @Override
    public final String name() {
        return propertyName;
    }

    public final T get() {
        return value;
    }

    public final void set(T newValue) {
        value = newValue;
    }

    public final CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        writeTo(nbt);
        return nbt;
    }

    public final CompoundTag toNbtWith(T newValue) {
        value = newValue;
        return toNbt();
    }

    public static final class IllegalPropertyStateException extends RuntimeException {
        public IllegalPropertyStateException(String message) {
            super(message);
        }
    }
}
