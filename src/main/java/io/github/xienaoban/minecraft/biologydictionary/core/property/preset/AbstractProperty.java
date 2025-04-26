package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityProperty;
import net.minecraft.world.entity.Entity;

import java.lang.invoke.VarHandle;

public abstract class AbstractProperty<E extends Entity, T> implements EntityProperty<E> {
    private final String propertyName;
    private T value;


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
        VarHandle.storeStoreFence();
        value = newValue;
    }

    public static final class IllegalPropertyStateException extends RuntimeException {
        public IllegalPropertyStateException(String message) {
            super(message);
        }
    }
}
