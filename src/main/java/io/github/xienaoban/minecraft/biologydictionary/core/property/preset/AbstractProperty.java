package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityVanillaProperty;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import sun.misc.Unsafe;

@Environment(EnvType.CLIENT)
public abstract class AbstractProperty<T> implements EntityVanillaProperty {
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
}
