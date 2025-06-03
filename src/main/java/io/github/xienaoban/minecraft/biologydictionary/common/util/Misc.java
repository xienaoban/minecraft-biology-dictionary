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

    public static <T> Collection<T> shuffle(Collection<T> collection) {
        ArrayList<T> list = new ArrayList<>(collection);
        Collections.shuffle(list);
        return list;
    }

    /**
     * Format num to sth. like 100, 10.0, 1.000
     * Only accept positive values.
     */
    public static String format3Digits(double num) {
        if (num >= 100) {
            return String.valueOf(Math.round(num));
        } else if (num >= 10) {
            return String.format("%.1f", num);
        } else if (num >= 0) {
            return String.format("%.2f", num);
        } else if (num > -10) {
            return String.format("%.1f", num);
        } else {
            return String.valueOf(Math.round(num));
        }
    }

    /**
     * Format num to sth. like 1000, 100.0, 10.00, 1.0000
     * Only accept positive values.
     */
    public static String format4Digits(double num) {
        if (num >= 1000) {
            return String.valueOf(Math.round(num));
        } else if (num >= 100) {
            return String.format("%.1f", num);
        } else if (num >= 10) {
            return String.format("%.2f", num);
        } else if (num >= 0) {
            return String.format("%.3f", num);
        } else if (num > -10) {
            return String.format("%.2f", num);
        } else if (num > -100) {
            return String.format("%.1f", num);
        } else {
            return String.valueOf(Math.round(num));
        }
    }
}
