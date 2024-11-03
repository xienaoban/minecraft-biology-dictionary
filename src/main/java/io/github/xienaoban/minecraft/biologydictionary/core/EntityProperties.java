package io.github.xienaoban.minecraft.biologydictionary.core;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityProperty;
import io.github.xienaoban.minecraft.biologydictionary.platform.access.EntityApi;
import io.github.xienaoban.minecraft.biologydictionary.util.Misc;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public final class EntityProperties<E extends Entity> {
    private final E entity;

    private final Map<Class<? extends EntityProperty<? super E>>, EntityProperty<? super E>> properties;

    public EntityProperties(E entity) {
        HashMap<Class<? extends EntityProperty<? super E>>, EntityProperty<? super E>> map = new HashMap<>();
        for (var clazz : EntityApi.bottomUp(entity)) {

        }
        this.entity = entity;
        this.properties = Collections.unmodifiableMap(map);
    }

    public E entity() { return entity; }

    public <EP extends EntityProperty<? super E>> EP p(Class<EP> clazz) {
        return Misc.cast(properties.get(clazz));
    }

    public void update(CompoundTag vanillaNbt, CompoundTag additionalNbt) {
        for (EntityProperty<? super E> property : properties.values()) {
            property.readFromNbt(vanillaNbt, additionalNbt);
        }
    }
}
