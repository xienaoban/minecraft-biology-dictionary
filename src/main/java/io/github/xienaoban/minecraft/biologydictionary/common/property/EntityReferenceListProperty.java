package io.github.xienaoban.minecraft.biologydictionary.common.property;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;

import java.util.List;

public final class EntityReferenceListProperty<E extends Entity> extends AbstractProperty<E, List<EntityReference<Entity>>> {
    public EntityReferenceListProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        set(nbt.read(name(), EntityReference.<Entity>codec().listOf()).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        nbt.storeNullable(name(), EntityReference.<Entity>codec().listOf(), get());
    }
}
