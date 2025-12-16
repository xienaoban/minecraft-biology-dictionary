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

    static final Map<Class<? extends Entity>, List<EntityProperty.Factory<?>>> registry = new HashMap<>();

    private static void registerBuiltIn() {
        r(MobTemptProperty.class, MobTemptProperty.FACTORY);
        r(MobNaturalPersistenceProperty.class, MobNaturalPersistenceProperty.FACTORY);
        r(VillagerJobSiteProperty.class, VillagerJobSiteProperty.FACTORY);
    }

    static void init() {
        registerBuiltIn();
    }

    static void r(Class<? extends EntityProperty<? extends Entity>> propertyClazz, EntityProperty.Factory<?> factory) {
        final Class<? extends Entity> entityClazz = Misc.getClazzGeneric(propertyClazz, EntityProperty.class, 0)
                .asSubclass(Entity.class);
        if (!propertyClazz.getSimpleName().startsWith(entityClazz.getSimpleName())) {
            throw new AssertionError(propertyClazz + " must be started with \""
                    + entityClazz.getSimpleName() + "\"!");
        }
        registry.computeIfAbsent(entityClazz, c -> new ArrayList<>()).add(factory);
    }
}
