package io.github.xienaoban.minecraft.biologydictionary.core.property;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityProperty;
import io.github.xienaoban.minecraft.biologydictionary.common.util.EntityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class EntityProperties<E extends Entity> {

    private final E entity;

    private final Map<String, EntityProperty<?>> vanillaProperties;

    public EntityProperties(E entity) {
        Map<String, EntityProperty<?>> map = new HashMap<>();
        try {
            for (var clazz : EntityUtils.topDown(entity)) {
                EntityVanillaProperties.Registry r = EntityVanillaProperties.registries
                        .getOrDefault(clazz, null);
                if (r != null) {
                    r.register(map);
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        this.entity = entity;
        this.vanillaProperties = Collections.unmodifiableMap(map);
    }

    public E entity() { return entity; }

    public Map<String, EntityProperty<?>> m() { return vanillaProperties; }

    public void update(CompoundTag vanillaNbt, CompoundTag extraNbt) {
        for (EntityProperty<?> property : vanillaProperties.values()) {
            property.readFrom(vanillaNbt);
        }
    }
}
