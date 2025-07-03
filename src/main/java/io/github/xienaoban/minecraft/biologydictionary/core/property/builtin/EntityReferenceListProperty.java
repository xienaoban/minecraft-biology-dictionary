package io.github.xienaoban.minecraft.biologydictionary.core.property.builtin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;

import java.util.List;

public final class EntityReferenceListProperty<E extends Entity> extends CodecProperty<E, List<EntityReference<Entity>>> {
    public EntityReferenceListProperty(String propertyName) {
        super(propertyName, EntityReference.<Entity>codec().listOf());
    }
}
