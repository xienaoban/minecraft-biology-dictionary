package io.github.xienaoban.minecraft.biologydictionary.core;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityPropertyWidgetRegistrar;
import io.github.xienaoban.minecraft.biologydictionary.api.EntityPropertyWidgetRegistry;
import io.github.xienaoban.minecraft.biologydictionary.core.tree.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public final class EntityPropertyWidgetRegistryManager implements EntityPropertyWidgetRegistrar {
    private static final EntityPropertyWidgetRegistryManager INSTANCE = new EntityPropertyWidgetRegistryManager();

    public static EntityPropertyWidgetRegistryManager getInstance() { return INSTANCE; }

    public static void init() {
        getInstance().registerDefaultEntityPropertyWidgets();
        getInstance().clearCache();
    }

    private final List<EntityPropertyWidgetRegistry<?>> registries;
    private Set<Class<?>> visited;

    private EntityPropertyWidgetRegistryManager() {
        registries = new ArrayList<>();
        visited = new HashSet<>();
    }

    public void register(EntityPropertyWidgetRegistry<?> registry) {
        registries.add(registry);
        if (!visited.add(registry.getClass())) {
            throw new RuntimeException(registry.getClass().getName() + " is already registered!");
        }
    }

    private void clearCache() {
        visited = null;
    }

    public List<EntityPropertyWidgetRegistry<?>> getRegistries() {
        return registries;
    }

    private void registerDefaultEntityPropertyWidgets() {
        register(new EntityImageWidgetRegistry());
        register(new LivingEntityHealthWidgetRegistry());
        register(new EntityAirWidgetRegistry());
        register(new AnimalFoodWidgetRegistry());
        register(new EntityBoundingBoxWidgetRegistry());
    }
}