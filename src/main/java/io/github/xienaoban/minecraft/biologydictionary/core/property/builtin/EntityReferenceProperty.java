package io.github.xienaoban.minecraft.biologydictionary.core.property.builtin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;

/**
 * @see net.minecraft.world.entity.EntityReference
 */
public class EntityReferenceProperty<E extends Entity> extends CodecProperty<E, EntityReference<Entity>> {
    public EntityReferenceProperty(String propertyName) {
        super(propertyName, EntityReference.codec());
    }
}
