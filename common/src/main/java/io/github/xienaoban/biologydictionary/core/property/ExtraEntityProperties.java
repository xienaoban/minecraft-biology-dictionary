package io.github.xienaoban.biologydictionary.core.property;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.api.ExtraEntityPropertiesPlugin;
import io.github.xienaoban.biologydictionary.api.ExtraEntityPropertiesRegistrar;
import io.github.xienaoban.biologydictionary.core.property.extra.*;
import io.github.xienaoban.biologydictionary.platform.PluginLookup;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ExtraEntityProperties {

    public static void registerBuiltIn(ExtraEntityPropertiesRegistrar registrar) {
        registrar.register(EntityLootTableProperty.class, EntityLootTableProperty.FACTORY);
        registrar.register(EntitySpawnCountedProperty.class, EntitySpawnCountedProperty.FACTORY);
        registrar.register(EntityInventorySizeProperty.class, EntityInventorySizeProperty.FACTORY);
        registrar.register(MobNaturalPersistenceProperty.class, MobNaturalPersistenceProperty.FACTORY);
        registrar.register(MobTemptProperty.class, MobTemptProperty.FACTORY);
        registrar.register(MobSpawnProperty.class, MobSpawnProperty.FACTORY);
        registrar.register(VillagerJobSiteProperty.class, VillagerJobSiteProperty.FACTORY);
    }

    static final Map<Class<? extends Entity>, List<EntityProperty.Factory<?>>> registry = new HashMap<>();

    static void init() {
        ExtraEntityPropertiesRegistrar registrar = new ExtraEntityPropertiesRegistrar() {
            @Override
            public <E extends Entity> void register(Class<? extends EntityProperty<E>> propertyClazz,
                    EntityProperty.Factory<E> factory) {
                final Class<? extends Entity> entityClazz
                        = Misc.getClazzGeneric(propertyClazz, EntityProperty.class, 0).asSubclass(Entity.class);
                registry.computeIfAbsent(entityClazz, c -> new ArrayList<>()).add(factory);
            }
        };

        registerBuiltIn(registrar);
        for (ExtraEntityPropertiesPlugin plugin : PluginLookup.find(ExtraEntityPropertiesPlugin.class)) {
            try {
                plugin.registerExtraEntityProperties(registrar);
            } catch (RuntimeException e) {
                throw new IllegalStateException("Failed to register properties from plugin "
                        + plugin.getClass().getName(), e);
            }
        }
    }
}
