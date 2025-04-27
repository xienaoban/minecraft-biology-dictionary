package io.github.xienaoban.minecraft.biologydictionary.core;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityProperty;
import io.github.xienaoban.minecraft.biologydictionary.core.property.VanillaProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.property.VanillaPropertiesUtils;
import io.github.xienaoban.minecraft.biologydictionary.common.util.EntityUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;

@Environment(EnvType.CLIENT)
public final class EntityProperties<E extends Entity> {
    private static final Map<Class<? extends Entity>, VanillaPropertiesUtils> vanillaRegisters = new ConcurrentHashMap<>();

    private static VanillaPropertiesUtils getRegister(Class<? extends Entity> entityClazz) {
        return vanillaRegisters.computeIfAbsent(entityClazz, cl -> {
            String clazzName = EntityUtils.getDeobfuscatedName(cl);
            if (clazzName == null) {
                return null;
            }
            String simpleName = clazzName.substring(clazzName.lastIndexOf('.') + 1);
            String ofName = VanillaProperties.class.getName() + "$Of" + simpleName;
            try {
                Class<?> ofClazz = Class.forName(ofName);
                return (VanillaPropertiesUtils) ofClazz.getConstructor().newInstance();

            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                     InvocationTargetException | InstantiationException e) {
                LOGGER.error("Failed to get the register method from " + ofName + ": " + e);
                return null;
            }
        });
    }

    private final E entity;

    private final Map<String, EntityProperty<?>> vanillaProperties;

    public EntityProperties(E entity) {
        Map<String, EntityProperty<?>> map = new HashMap<>();
        try {
            for (var clazz : EntityUtils.topDown(entity)) {
                VanillaPropertiesUtils register = getRegister(clazz);
                if (register != null) {
                    register.register(map);
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
