package io.github.xienaoban.minecraft.biologydictionary.common.property;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;

import java.util.List;

public final class EntityReferenceListProperty<E extends Entity> extends CodecProperty<E, List<EntityReference<Entity>>> {
    public EntityReferenceListProperty(String propertyName) {
        super(propertyName, EntityReference.<Entity>codec().listOf());
    }
}
