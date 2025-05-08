package io.github.xienaoban.minecraft.biologydictionary.core.property;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityProperty;
import io.github.xienaoban.minecraft.biologydictionary.common.util.EntityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public final class EntityProperties<E extends Entity> {

    private final E entity;

    private final Map<String, EntityProperty<?>> vanillaProperties;
    private final Map<Class<? extends EntityProperty>, EntityProperty<?>> extraProperties;

    public EntityProperties(E entity) {
        this.entity = entity;

        final var vRegs = EntityVanillaProperties.registries;
        final var eRegs = EntityExtraProperties.registries;

        Map<String, EntityProperty<?>> vMap = new HashMap<>();
        Map<Class<? extends EntityProperty>, EntityProperty<?>> eMap = new HashMap<>();
        for (var clazz : EntityUtils.topDown(entity)) {
            EntityVanillaProperties.Creator vc = vRegs.getOrDefault(clazz, null);
            if (vc != null) {
                vc.create(vMap);
            }

            for (EntityExtraProperties.Creator ec : eRegs.getOrDefault(clazz, Collections.emptyList())) {
                EntityProperty<?> p = ec.create();
                eMap.put(p.getClass(), p);
            }
        }
        this.vanillaProperties = Collections.unmodifiableMap(vMap);
        this.extraProperties = Collections.unmodifiableMap(eMap);
    }

    public E entity() { return entity; }

    public Map<String, EntityProperty<?>> m() { return vanillaProperties; }

    public EntityProperty<?> getVanilla(String key) {
        return vanillaProperties.getOrDefault(key, null);
    }

    public EntityProperty<?> getExtra(Class<? extends EntityProperty> key) {
        return extraProperties.getOrDefault(key, null);
    }

    public void update(CompoundTag vanillaNbt, CompoundTag extraNbt) {
        for (EntityProperty<?> property : vanillaProperties.values()) {
            property.readFrom(vanillaNbt);
        }
    }
}
