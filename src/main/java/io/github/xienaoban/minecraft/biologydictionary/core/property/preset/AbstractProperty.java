package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityProperty;

import java.lang.invoke.VarHandle;

public abstract class AbstractProperty<T> implements EntityProperty {
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
        value = newValue;
        VarHandle.releaseFence();
    }

    public static final class IllegalPropertyStateException extends RuntimeException {
        public IllegalPropertyStateException(String message) {
            super(message);
        }
    }
}
