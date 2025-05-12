package io.github.xienaoban.minecraft.biologydictionary.core.property;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityProperty;
import io.github.xienaoban.minecraft.biologydictionary.common.util.Misc;
import io.github.xienaoban.minecraft.biologydictionary.core.property.extra.MobTemptProperty;
import net.minecraft.world.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class EntityExtraProperties {

    static final Map<Class<? extends Entity>, List<Creator>> registries = new HashMap<>();

    private static MethodHandles.Lookup lookup;

    @FunctionalInterface
    interface Creator {
        EntityProperty<?> create();
    }

    static {
        lookup = MethodHandles.lookup();
        registerBuiltIn();
        lookup = null;
    }

    static void r(Class<? extends EntityProperty<? extends Entity>> clazz) {
        try {
            final Class<? extends Entity> entityClazz = Misc.getFirstEntityClazzGeneric(clazz);
            MethodHandle constructor = lookup.findConstructor(clazz, MethodType.methodType(void.class));
            registries.computeIfAbsent(entityClazz, c -> new ArrayList<>())
                    .add(() -> {
                        try {
                            return (EntityProperty<?>) constructor.invoke();
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void registerBuiltIn() {
        r(MobTemptProperty.class);
    }
}
