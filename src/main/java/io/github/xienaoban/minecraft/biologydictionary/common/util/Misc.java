package io.github.xienaoban.minecraft.biologydictionary.common.util;

import net.minecraft.world.entity.Entity;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public final class Misc {
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    public static <E extends Entity> Class<E> getFirstEntityClazzGeneric(Class<?> clazz) {
        // Get the entity class (aka E) based on generic super class EntityPropertyWidget.
        ParameterizedType superWidgetType = (ParameterizedType) clazz.getGenericSuperclass();
        Class<?> c = (Class<?>) superWidgetType.getActualTypeArguments()[0];
        if (!Entity.class.isAssignableFrom(c)) {
            throw new RuntimeException("The first argument of generic super class is not a sub class of Entity.");
        }
        return Misc.cast(c);
    }

    public static  <T> Collection<T> shuffle(Collection<T> collection) {
        ArrayList<T> list = new ArrayList<>(collection);
        Collections.shuffle(list);
        return list;
    }
}
