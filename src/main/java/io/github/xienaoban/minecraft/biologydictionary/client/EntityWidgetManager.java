package io.github.xienaoban.minecraft.biologydictionary.client;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityWidgetRegistry;
import io.github.xienaoban.minecraft.biologydictionary.core.registry.EntityWidgetRegistryManager;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityWidget;
import io.github.xienaoban.minecraft.biologydictionary.util.MiscUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

import java.util.*;

@Environment(EnvType.CLIENT)
public final class EntityWidgetManager {
    private static final EntityWidgetManager INSTANCE = new EntityWidgetManager();

    public static EntityWidgetManager getInstance() { return INSTANCE; }

    public static void init() {
        EntityWidgetManager manager = getInstance();
        EntityWidgetRegistryManager.getInstance().getRegistries().forEach(manager::register);
    }

    private final Map<Class<? extends Entity>, List<EntityWidgetRegistry.EntityWidgetFactory<?>>> widgets;

    private EntityWidgetManager() {
        widgets = new HashMap<>();
    }

    private void register(EntityWidgetRegistry<?> registry) {
        register(registry.getEntityClass(), registry.getWidgetFactory());
    }

    private void register(Class<? extends Entity> entityClazz, EntityWidgetRegistry.EntityWidgetFactory<?> widgetFactory) {
        widgets.computeIfAbsent(entityClazz,  clazz ->  new ArrayList<>()).add(widgetFactory);
    }

    public List<EntityWidget<?>> getWidgets(Entity entity) {
        List<EntityWidget<?>> res = new ArrayList<>();
        Deque<Class<?>> stack = new ArrayDeque<>();
        Class<?> clazz = entity.getClass();
        while (clazz != Entity.class) {
            stack.addFirst(clazz);
            clazz = clazz.getSuperclass();
        }
        stack.addFirst(Entity.class);
        while ((clazz = stack.pollFirst()) != null) {
            widgets.getOrDefault(clazz, Collections.emptyList()).forEach(factory -> {
                EntityWidget<?> widget = factory.create(MiscUtil.cast(entity));
                res.add(widget);
            });
        }
        return res;
    }
}
