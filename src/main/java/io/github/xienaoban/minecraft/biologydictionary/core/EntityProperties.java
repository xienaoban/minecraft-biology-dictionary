package io.github.xienaoban.minecraft.biologydictionary.core;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityProperty;
import io.github.xienaoban.minecraft.biologydictionary.core.property.VanillaProperties;
import io.github.xienaoban.minecraft.biologydictionary.util.EntityUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public final class EntityProperties<E extends Entity> {
    private static final MethodHandle[] EMPTY_METHOD_HANDLES = new MethodHandle[0];

    private static final Map<Class<? extends Entity>, MethodHandle[]> creatorHandles = new ConcurrentHashMap<>();

    private static MethodHandle[] getCreatorHandles(Class<? extends Entity> entityClazz) {
        return creatorHandles.computeIfAbsent(entityClazz, cl -> {
            String clazzName = EntityUtils.getDeobfuscatedName(cl);
            if (clazzName == null) {
                return EMPTY_METHOD_HANDLES;
            }
            String simpleName = clazzName.substring(clazzName.lastIndexOf('.') + 1);
            String ofName = VanillaProperties.class.getName() + "$Of" + simpleName;
            try {
                Class<?> ofClazz = Class.forName(ofName);
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                return Arrays.stream(ofClazz.getMethods())
                        .filter(method -> method.getName().startsWith("create"))
                        .map(method -> {
                            try {
                                return lookup.unreflect(method);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .toArray(MethodHandle[]::new);
            } catch (ClassNotFoundException e) {
                return EMPTY_METHOD_HANDLES;
            }
        });
    }

    private final E entity;

    private final Map<String, EntityProperty> vanillaProperties;

    public EntityProperties(E entity) {
        HashMap<String, EntityProperty> map = new HashMap<>();
        try {
            for (var clazz : EntityUtils.topDown(entity)) {
                for (MethodHandle mh : getCreatorHandles(clazz)) {
                    EntityProperty property = (EntityProperty) mh.invoke();
                    map.put(property.name(), property);
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
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
