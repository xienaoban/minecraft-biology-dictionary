package io.github.xienaoban.minecraft.biologydictionary.core.property;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityProperty;
import io.github.xienaoban.minecraft.biologydictionary.common.util.Misc;
import io.github.xienaoban.minecraft.biologydictionary.core.property.extra.MobTemptProperty;
import net.minecraft.world.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class EntityExtraProperties {
    private static final Map<Class<? extends Entity>, List<EntityExtraPropertyRegistry>> properties = new HashMap<>();
    private static MethodHandles.Lookup lookup;

    static {
        lookup = MethodHandles.lookup();
        init();
        lookup = null;
    }

    static void r(Class<? extends EntityProperty<? extends Entity>> clazz) {
        final Class<? extends Entity> entityClazz;
        final MethodHandle createWidget;
        try {
            entityClazz = Misc.getFirstEntityClazzGeneric(clazz);
            MethodHandle constructorHandle = lookup.findConstructor(clazz, MethodType.methodType(void.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void init() {
        r(MobTemptProperty.class);
    }
}
