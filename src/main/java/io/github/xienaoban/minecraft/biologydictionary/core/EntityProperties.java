package io.github.xienaoban.minecraft.biologydictionary.core;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityProperty;
import io.github.xienaoban.minecraft.biologydictionary.util.EntityUtils;
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

    private final Map<String, EntityProperty> vanillaProperties;

    public EntityProperties(E entity) {
        HashMap<String, EntityProperty> map = new HashMap<>();
        for (var clazz : EntityUtils.bottomUp(entity)) {

        }
        this.entity = entity;
        this.vanillaProperties = Collections.unmodifiableMap(map);
    }

    public E entity() { return entity; }

    public Map<String, EntityProperty> m() { return vanillaProperties; }

    public void update(CompoundTag vanillaNbt, CompoundTag extraNbt) {
        for (EntityProperty property : vanillaProperties.values()) {
            property.readFrom(vanillaNbt);
        }
    }
}
