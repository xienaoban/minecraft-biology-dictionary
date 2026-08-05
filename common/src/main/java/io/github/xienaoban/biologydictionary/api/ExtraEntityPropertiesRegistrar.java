package io.github.xienaoban.biologydictionary.api;

import io.github.xienaoban.biologydictionary.core.property.EntityProperty;
import net.minecraft.world.entity.Entity;

/**
 * Registration handle for the extra-entity-property registry, passed to {@link ExtraEntityPropertiesPlugin}.
 */
public interface ExtraEntityPropertiesRegistrar {
    <E extends Entity> void register(Class<? extends EntityProperty<E>> propertyClazz,
                                     EntityProperty.Factory<E> factory);
}
