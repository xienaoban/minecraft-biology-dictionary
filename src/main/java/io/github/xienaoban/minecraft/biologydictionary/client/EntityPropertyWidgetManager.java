package io.github.xienaoban.minecraft.biologydictionary.client;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityPropertyWidgetRegistry;
import io.github.xienaoban.minecraft.biologydictionary.core.registry.EntityPropertyWidgetRegistryManager;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.minecraft.biologydictionary.util.MiscUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

import java.util.*;

@Environment(EnvType.CLIENT)
public final class EntityPropertyWidgetManager {
    private static final EntityPropertyWidgetManager INSTANCE = new EntityPropertyWidgetManager();

    public static EntityPropertyWidgetManager getInstance() { return INSTANCE; }

    public static void init() {
        EntityPropertyWidgetManager manager = getInstance();
        EntityPropertyWidgetRegistryManager.getInstance().getRegistries().forEach(manager::register);
    }

    private final Map<Class<? extends Entity>, List<EntityPropertyWidgetRegistry.EntityPropertyWidgetFactory<?>>> widgets;

    private EntityPropertyWidgetManager() {
        widgets = new HashMap<>();
    }

    private void register(EntityPropertyWidgetRegistry<?> registry) {
        register(registry.getEntityClass(), registry.getWidgetFactory());
    }

    private void register(Class<? extends Entity> entityClazz, EntityPropertyWidgetRegistry.EntityPropertyWidgetFactory<?> widgetFactory) {
        widgets.computeIfAbsent(entityClazz,  clazz ->  new ArrayList<>()).add(widgetFactory);
    }

    public List<EntityPropertyWidget<?>> getWidgets(Entity entity) {
        List<EntityPropertyWidget<?>> res = new ArrayList<>();
        Deque<Class<?>> stack = new ArrayDeque<>();
        Class<?> clazz = entity.getClass();
        while (clazz != Entity.class) {
            stack.addFirst(clazz);
            clazz = clazz.getSuperclass();
        }
        stack.addFirst(Entity.class);
        while ((clazz = stack.pollFirst()) != null) {
            widgets.getOrDefault(clazz, Collections.emptyList()).forEach(factory -> {
                EntityPropertyWidget<?> widget = factory.create(MiscUtil.cast(entity));
                res.add(widget);
            });
        }
        return res;
    }
}
