package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityProperty;
import sun.misc.Unsafe;

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
        Unsafe.getUnsafe().storeFence();
        value = newValue;
    }

    public static final class IllegalPropertyStateException extends RuntimeException {
        public IllegalPropertyStateException(String message) {
            super(message);
        }
    }
}
