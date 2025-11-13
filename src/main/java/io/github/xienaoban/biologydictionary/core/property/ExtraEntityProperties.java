package io.github.xienaoban.biologydictionary.core.property;

import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.property.extra.*;
import net.minecraft.world.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ExtraEntityProperties {

    static final Map<Class<? extends Entity>, List<Creator>> registries = new HashMap<>();

    @FunctionalInterface
    interface Creator {
        EntityProperty<?> create();
    }

    static void init() {
        registerBuiltIn();
    }

    static void r(Class<? extends EntityProperty<? extends Entity>> propertyClazz) {
        try {
            final Class<? extends Entity> entityClazz= Misc.getClazzGeneric(propertyClazz, EntityProperty.class, 0)
                    .asSubclass(Entity.class);
            if (!propertyClazz.getSimpleName().startsWith(entityClazz.getSimpleName())) {
                throw new AssertionError(propertyClazz + " must be started with \"" + entityClazz.getSimpleName() + "\"!");
            }

            MethodHandle constructor = MethodHandles.lookup().findConstructor(propertyClazz, MethodType.methodType(void.class));
            registries.computeIfAbsent(entityClazz, c -> new ArrayList<>())
                    .add(() -> {
                        try {
                            return (EntityProperty<?>) constructor.invoke();
                        } catch (RuntimeException e) {
                            throw e;
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
        r(MobNaturalPersistenceProperty.class);
        r(VillagerJobSiteProperty.class);
    }
}
