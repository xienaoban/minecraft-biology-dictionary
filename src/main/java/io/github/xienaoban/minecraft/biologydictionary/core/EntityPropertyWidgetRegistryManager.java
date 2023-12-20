package io.github.xienaoban.minecraft.biologydictionary.core;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityPropertyWidgetRegistrar;
import io.github.xienaoban.minecraft.biologydictionary.api.EntityPropertyWidgetRegistry;
import io.github.xienaoban.minecraft.biologydictionary.core.tree.*;
import net.minecraft.world.entity.Entity;

import java.util.*;


public final class EntityPropertyWidgetRegistryManager implements EntityPropertyWidgetRegistrar {
    private static final EntityPropertyWidgetRegistryManager INSTANCE = new EntityPropertyWidgetRegistryManager();

    public static EntityPropertyWidgetRegistryManager getInstance() { return INSTANCE; }

    public static void init() {
        getInstance().registerDefaultEntityPropertyWidgets();
        getInstance().clearCache();
    }

    private final Map<Class<? extends Entity>, List<EntityPropertyWidgetRegistry<?>>> registries;
    private Set<Class<?>> visited;

    private EntityPropertyWidgetRegistryManager() {
        this.registries = new HashMap<>();
        this.visited = new HashSet<>();
    }

    public void register(EntityPropertyWidgetRegistry<?> registry) {
        registries.computeIfAbsent(registry.getEntityClass(), clazz -> new ArrayList<>()).add(registry);
        if (!visited.add(registry.getClass())) {
            throw new IllegalStateException(registry.getClass().getName() + " is already registered!");
        }
    }

    public List<EntityPropertyWidgetRegistry<?>> getRegistries(Class<? extends Entity> clazz) {
        return registries.getOrDefault(clazz, Collections.emptyList());
    }

    private void clearCache() {
        visited = null;
    }

    private void registerDefaultEntityPropertyWidgets() {
        register(new EntityImageWidgetRegistry());
        register(new LivingEntityHealthWidgetRegistry());
        register(new EntityAirWidgetRegistry());
        register(new AnimalFoodWidgetRegistry());
        register(new EntityBoundingBoxWidgetRegistry());
    }
}