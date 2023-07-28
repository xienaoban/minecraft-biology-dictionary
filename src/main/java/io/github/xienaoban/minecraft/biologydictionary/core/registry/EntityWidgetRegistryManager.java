package io.github.xienaoban.minecraft.biologydictionary.core.registry;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityWidgetRegistrar;
import io.github.xienaoban.minecraft.biologydictionary.api.EntityWidgetRegistry;
import io.github.xienaoban.minecraft.biologydictionary.core.registry.tree.EntityImageWidgetRegistry;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public final class EntityWidgetRegistryManager implements EntityWidgetRegistrar {
    private static final EntityWidgetRegistryManager INSTANCE = new EntityWidgetRegistryManager();

    public static EntityWidgetRegistryManager getInstance() { return INSTANCE; }

    public static void init() {
        getInstance().registerDefaultEntityWidgets();
        getInstance().clearCache();
    }

    private final List<EntityWidgetRegistry<? extends Entity>> registries;
    private Set<Class<?>> visited;

    private EntityWidgetRegistryManager() {
        registries = new ArrayList<>();
        visited = new HashSet<>();
    }

    public void register(EntityWidgetRegistry<? extends Entity> registry) {
        registries.add(registry);
        if (!visited.add(registry.getClass())) {
            throw new RuntimeException(registry.getClass().getName() + " is already registered!");
        }
    }

    private void clearCache() {
        visited = null;
    }

    public List<EntityWidgetRegistry<? extends Entity>> getRegistries() {
        return registries;
    }

    private void registerDefaultEntityWidgets() {
        register(new EntityImageWidgetRegistry());
    }
}