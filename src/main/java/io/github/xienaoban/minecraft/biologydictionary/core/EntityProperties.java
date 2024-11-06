package io.github.xienaoban.minecraft.biologydictionary.core;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityVanillaProperty;
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

    private final Map<Class<? extends EntityVanillaProperty<? super E>>, EntityVanillaProperty<? super E>> vanillaProperties;

    public EntityProperties(E entity) {
        HashMap<Class<? extends EntityVanillaProperty<? super E>>, EntityVanillaProperty<? super E>> map = new HashMap<>();
        for (var clazz : EntityApi.bottomUp(entity)) {

        }
        this.entity = entity;
        this.vanillaProperties = Collections.unmodifiableMap(map);
    }

    public E entity() { return entity; }

    public <EP extends EntityVanillaProperty<? super E>> EP p(Class<EP> clazz) {
        return Misc.cast(vanillaProperties.get(clazz));
    }

    public void update(CompoundTag vanillaNbt, CompoundTag extraNbt) {
        for (EntityVanillaProperty<? super E> property : vanillaProperties.values()) {
            property.readFrom(vanillaNbt);
        }
    }
}
