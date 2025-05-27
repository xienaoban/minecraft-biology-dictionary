package io.github.xienaoban.minecraft.biologydictionary.common.util;

import io.github.xienaoban.minecraft.biologydictionary.mixin.EntityIMixin;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EntityUtils {
    public static void init() {
        EntityVanillaDeobfuscation.clazzToName.get(null);
    }

    public static <E extends Entity> E create(EntityType<E> entityType) {
        return create(entityType, MinecraftUtils.getLocalLevel());
    }

    public static <E extends Entity> E create(EntityType<E> entityType, Level level) {
        return entityType.create(level, null);
    }

    public static void setInWater(Entity entity, boolean inWater) {
        ((EntityIMixin) entity).setWasTouchingWater(inWater);
    }

    /**
     * Can be used in client side.
     */
    public static boolean isBaby(AgeableMob entity) {
        return entity.isBaby();
    }

    public static List<Class<? extends Entity>> topDown(Entity entity) {
        List<Class<? extends Entity>> list = bottomUp(entity);
        Collections.reverse(list);
        return list;
    }

    public static List<Class<? extends Entity>> bottomUp(Entity entity) {
        List<Class<? extends Entity>> list = new ArrayList<>();
        Class<? extends Entity> clazz = entity.getClass();
        while (clazz != Entity.class) {
            list.add(clazz);
            clazz = clazz.getSuperclass().asSubclass(Entity.class);
        }
        list.add(Entity.class);
        return list;
    }

    public static List<Class<? extends Entity>> getVanillaEntityClazzes() {
        return EntityVanillaDeobfuscation.clazzes;
    }

    /**
     * Get deobfuscated class name of the vanilla entity.
     * @param clazz Entity class
     * @return deobfuscated class name or null if not vanilla entity class
     */
    public static String getDeobfuscatedName(Class<? extends Entity> clazz) {
        return EntityVanillaDeobfuscation.clazzToName.get(clazz);
    }
}
