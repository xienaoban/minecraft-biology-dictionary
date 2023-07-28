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

    private final Map<Class<? extends Entity>, List<EntityWidgetRegistry.EntityWidgetFactory<? extends Entity>>> widgets;

    private EntityWidgetManager() {
        widgets = new HashMap<>();
    }

    public <E extends Entity> void register(EntityWidgetRegistry<E> registry) {
        register(registry.getEntityClass(), registry.getWidgetFactory());
    }

    private <E extends Entity> void register(Class<E> entityClazz, EntityWidgetRegistry.EntityWidgetFactory<E> widgetFactory) {
        widgets.computeIfAbsent(entityClazz,  clazz ->  new ArrayList<>()).add(widgetFactory);
    }

//    public <E extends Entity> List<EntityWidget<? super E>> getWidgets(E entity) {
//        List<EntityWidget<? super E>> res = new ArrayList<>();
//        Deque<Class<? extends Entity>> stack = new ArrayDeque<>();
//        Class<? extends Entity> clazz = entity.getClass();
//        while (clazz != Entity.class) {
//            stack.addFirst(clazz);
//            clazz = MiscUtil.cast(clazz.getSuperclass());
//        }
//        stack.addFirst(Entity.class);
//        while ((clazz = stack.pollFirst()) != null) {
//            for (EntityWidgetFactory<? extends Entity> factory : widgets.getOrDefault(clazz, Collections.emptyList())) {
//                EntityWidgetFactory<? super E> trueTypeFactory = MiscUtil.cast(factory);
//                EntityWidget<? super E> widget = trueTypeFactory.create(entity);
//                res.add(widget);
//            }
//        }
//        return res;
//    }

    public List<EntityWidget<? extends Entity>> getWidgets(Entity entity) {
        List<EntityWidget<? extends Entity>> res = new ArrayList<>();
        Deque<Class<? extends Entity>> stack = new ArrayDeque<>();
        Class<? extends Entity> clazz = entity.getClass();
        while (clazz != Entity.class) {
            stack.addFirst(clazz);
            clazz = MiscUtil.cast(clazz.getSuperclass());
        }
        stack.addFirst(Entity.class);
        while ((clazz = stack.pollFirst()) != null) {
            widgets.getOrDefault(clazz, Collections.emptyList()).forEach(factory -> {
                EntityWidget<? extends Entity> widget = factory.create(MiscUtil.cast(entity));
                res.add(widget);
            });
        }
        return res;
    }
}
