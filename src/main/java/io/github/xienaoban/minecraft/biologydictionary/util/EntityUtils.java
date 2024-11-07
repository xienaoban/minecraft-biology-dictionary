package io.github.xienaoban.minecraft.biologydictionary.util;

import io.github.xienaoban.minecraft.biologydictionary.platform.mixin.EntityIMixin;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EntityUtils {
    public static void init() {
        AutoGenEntityDeobfuscation.map.get(null);
    }

    public static void setInWater(Entity entity, boolean inWater) {
        ((EntityIMixin) entity).setWasTouchingWater(inWater);
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

    /**
     * Get deobfuscated class name of the vanilla entity.
     */
    public static String getDeobfuscatedName(Class<?> clazz) {
        return AutoGenEntityDeobfuscation.map.get(clazz);
    }
}
