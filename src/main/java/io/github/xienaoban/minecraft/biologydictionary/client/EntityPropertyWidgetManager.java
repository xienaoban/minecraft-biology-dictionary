package io.github.xienaoban.minecraft.biologydictionary.client;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityPropertyWidgetRegister;
import io.github.xienaoban.minecraft.biologydictionary.core.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.widget.*;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.minecraft.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.minecraft.biologydictionary.common.util.Misc;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.*;

@Environment(EnvType.CLIENT)
public final class EntityPropertyWidgetManager implements EntityPropertyWidgetRegister {
    private static final EntityPropertyWidgetManager INSTANCE = new EntityPropertyWidgetManager();

    public static EntityPropertyWidgetManager getInstance() { return INSTANCE; }

    public static void init() {
        getInstance().registerDefaultEntityPropertyWidgets();
        getInstance().clearCache();
    }

    private final Map<Class<? extends Entity>, List<WidgetRegistry<?>>> registries;

    private Set<Class<?>> visited;
    private MethodHandles.Lookup lookup;

    private EntityPropertyWidgetManager() {
        this.registries = new HashMap<>();

        this.visited = new HashSet<>();
        this.lookup = MethodHandles.lookup();
    }

    public List<EntityPropertyWidget<?>> getWidgets(EntityProperties<?> properties) {
        List<EntityPropertyWidget<?>> res = new ArrayList<>();
        for (var clazz : EntityUtils.topDown(properties.entity())) {
            for (var registry : getRegistries(clazz)) {
                EntityPropertyWidget<?> widget = registry.createWidget(Misc.cast(properties));
                res.add(widget);
            }
        }
        return res;
    }

    @Override
    public <E extends Entity> void register(Class<? extends EntityPropertyWidget<E>> widgetClazz) {
        if (!visited.add(widgetClazz)) {
            throw new IllegalStateException(widgetClazz + " is already registered!");
        }
        final Class<E> entityClazz;
        final MethodHandle createWidget;
        try {
            // Get the entity class (aka E) based on generic super class EntityPropertyWidget.
            ParameterizedType superWidgetType = (ParameterizedType) widgetClazz.getGenericSuperclass();
            Class<?> c = (Class<?>) superWidgetType.getActualTypeArguments()[0];
            if (!Entity.class.isAssignableFrom(c)) {
                throw new RuntimeException("The first argument of generic super class is not a sub class of Entity.");
            }
            entityClazz = Misc.cast(c);

            // Get the constructor of the widget class.
            Constructor<? extends EntityPropertyWidget<E>> constructor = widgetClazz.getDeclaredConstructor(EntityProperties.class);
            createWidget = lookup.unreflectConstructor(constructor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register " + widgetClazz, e);
        }

        // Register it.
        WidgetRegistry<E> registry = new WidgetRegistry<>() {
            @Override
            public Class<E> getEntityClass() {
                return entityClazz;
            }

            @Override
            public EntityPropertyWidget<E> createWidget(EntityProperties<E> properties) {
                try {
                    return Misc.cast(createWidget.invoke(properties));
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        };
        registries.computeIfAbsent(registry.getEntityClass(), clazz -> new ArrayList<>()).add(registry);
    }

    private List<WidgetRegistry<?>> getRegistries(Class<? extends Entity> clazz) {
        return registries.getOrDefault(clazz, Collections.emptyList());
    }

    private void clearCache() {
        visited = null;
        lookup = null;
    }

    private void registerDefaultEntityPropertyWidgets() {
        register(EntityImageWidget.class);
        register(LivingEntityHealthWidget.class);
        register(EntityAirWidget.class);
        register(EntityPortalCooldownWidget.class);
        register(AnimalFoodWidget.class);
        register(EntityLeashableWidget.class);
        register(EntityBoundingBoxWidget.class);
    }

    private interface WidgetRegistry<E extends Entity> {
        Class<E> getEntityClass();

        @Environment(EnvType.CLIENT)
        EntityPropertyWidget<E> createWidget(EntityProperties<E> properties);
    }
}