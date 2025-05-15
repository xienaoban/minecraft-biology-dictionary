package io.github.xienaoban.minecraft.biologydictionary.core.widget;

import io.github.xienaoban.minecraft.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.minecraft.biologydictionary.common.util.Misc;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.widget.impl.*;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.*;

@Environment(EnvType.CLIENT)
public final class EntityPropertyWidgets {

    private static final Map<Class<? extends Entity>, List<Creator<?>>> registries = new HashMap<>();
    private static final Map<Class<? extends EntityPropertyWidget<?>>, Integer> order = new HashMap<>();

    private static int orderIndex;
    private static Set<Class<?>> visited;
    private static MethodHandles.Lookup lookup;

    private static final Comparator<EntityPropertyWidget<?>> CMP = Comparator.comparingInt(o -> order.get(o.getClass()));

    public static void init() {
        orderIndex = 0;
        visited = new HashSet<>();
        lookup = MethodHandles.lookup();
        registerBuiltIn();
        orderIndex = -1;
        visited = null;
        lookup = null;
    }

    @FunctionalInterface
    private interface Creator<E extends Entity> {
        EntityPropertyWidget<E> create(EntityProperties<E> properties);
    }

    public static List<EntityPropertyWidget<?>> getWidgets(EntityProperties<?> properties) {
        List<EntityPropertyWidget<?>> res = new ArrayList<>();
        for (var clazz : EntityUtils.topDown(properties.entity())) {
            for (var creator : getCreators(clazz)) {
                EntityPropertyWidget<?> widget = creator.create(Misc.cast(properties));
                res.add(widget);
            }
        }
        res.sort(CMP);
        return res;
    }

    private static <E extends Entity> void r(Class<? extends EntityPropertyWidget<E>> widgetClazz) {
        if (!visited.add(widgetClazz)) {
            throw new IllegalStateException(widgetClazz + " is already registered!");
        }
        final Class<E> entityClazz;
        final MethodHandle createWidget;
        try {
            entityClazz = Misc.getFirstEntityClazzGeneric(widgetClazz);

            // Get the constructor of the widget class.
            Constructor<? extends EntityPropertyWidget<E>> constructor = widgetClazz.getDeclaredConstructor(EntityProperties.class);
            createWidget = lookup.unreflectConstructor(constructor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register " + widgetClazz, e);
        }

        // Register it.
        Creator<E> creator = properties -> {
            try {
                return Misc.cast(createWidget.invoke(properties));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
        registries.computeIfAbsent(entityClazz, clazz -> new ArrayList<>()).add(creator);
        if (order.put(widgetClazz, orderIndex++) != null) {
            throw new RuntimeException("Duplicate registered widget: " + widgetClazz);
        }
    }

    private static List<Creator<?>> getCreators(Class<? extends Entity> clazz) {
        return registries.getOrDefault(clazz, Collections.emptyList());
    }

    private static void registerBuiltIn() {
        r(EntityImageWidget.class);
        r(LivingEntityHealthWidget.class);
        r(EntityAirWidget.class);
        r(LivingEntityActiveEffectsWidget.class);
        r(AnimalFoodWidget.class);
        r(MobTemptWidget.class);
        r(EntityLeashableWidget.class);
        r(EntityBoundingBoxWidget.class);
        r(EntityInvulnerableWidget.class);
        r(EntitySilentWidget.class);
        r(EntityPortalCooldownWidget.class);
    }
}