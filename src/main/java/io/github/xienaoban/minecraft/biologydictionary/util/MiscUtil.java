package io.github.xienaoban.minecraft.biologydictionary.util;

import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MiscUtil {
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    public static boolean isVanillaClass(Class<?> clazz) {
        return clazz.getPackage().getName().startsWith("net.minecraft");
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
            clazz = cast(clazz.getSuperclass());
        }
        list.add(Entity.class);
        return list;
    }
}
