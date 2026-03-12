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

    public final T getVal() {
        return value;
    }

    public final void setVal(T newValue) {
        value = newValue;
    }

    public final AbstractProperty<E, T> withVal(T newValue) {
        setVal(newValue);
        return this;
    }

    public final AbstractProperty<E, T> withEntity(E entity) {
        getFrom(entity);
        return this;
    }

    public final AbstractProperty<E, T> withTag(CompoundTag nbt) {
        readFrom(nbt);
        return this;
    }

    public final CompoundTag toTag() {
        CompoundTag nbt = new CompoundTag();
        writeTo(nbt);
        return nbt;
    }

    public static final class IllegalPropertyStateException extends RuntimeException {
        public IllegalPropertyStateException(String message) {
            super(message);
        }
    }
}
