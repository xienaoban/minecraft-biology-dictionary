package io.github.xienaoban.biologydictionary.core.property;

import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.property.extra.MobNaturalPersistenceProperty;
import io.github.xienaoban.biologydictionary.core.property.extra.MobTemptProperty;
import io.github.xienaoban.biologydictionary.core.property.extra.VillagerJobSiteProperty;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ExtraEntityProperties {

    public static void registerBuiltIn(Registrar registrar) {
        registrar.register(MobTemptProperty.class, MobTemptProperty.FACTORY);
        registrar.register(MobNaturalPersistenceProperty.class, MobNaturalPersistenceProperty.FACTORY);
        registrar.register(VillagerJobSiteProperty.class, VillagerJobSiteProperty.FACTORY);
    }

    static final Map<Class<? extends Entity>, List<EntityProperty.Factory<?>>> registry = new HashMap<>();

    static void init() {
        Registrar registrar = ExtraEntityProperties::register0;
        registerBuiltIn(registrar);
    }

    private static void register0(Class<? extends EntityProperty<? extends Entity>> propertyClazz,
                                  EntityProperty.Factory<?> factory) {
        final Class<? extends Entity> entityClazz
                = Misc.getClazzGeneric(propertyClazz, EntityProperty.class, 0).asSubclass(Entity.class);
        if (!propertyClazz.getSimpleName().startsWith(entityClazz.getSimpleName())) {
            throw new AssertionError(propertyClazz + " must be started with \""
                    + entityClazz.getSimpleName() + "\"!");
        }
        registry.computeIfAbsent(entityClazz, c -> new ArrayList<>()).add(factory);
    }

    @FunctionalInterface
    public interface Registrar {
        void register(Class<? extends EntityProperty<? extends Entity>> propertyClazz,
                      EntityProperty.Factory<?> factory);
    }
}
