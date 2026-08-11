package io.github.xienaoban.biologydictionary.api.plugin;

import io.github.xienaoban.biologydictionary.core.property.EntityProperty;
import net.minecraft.world.entity.Entity;

/**
 * Plugin for registering extra entity properties. Discovered and dispatched exactly once during
 * initialization, before the property registry freezes.
 */
public interface ExtraEntityPropertiesPlugin {
    void registerExtraEntityProperties(ExtraEntityPropertiesPlugin.Registrar registrar);

    interface Registrar {
        <E extends Entity> void register(Class<? extends EntityProperty<E>> propertyClazz,
                                         EntityProperty.Factory<E> factory);
    }
}
