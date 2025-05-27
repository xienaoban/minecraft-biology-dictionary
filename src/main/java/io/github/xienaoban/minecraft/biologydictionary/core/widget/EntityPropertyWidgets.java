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

    private static final Map<Class<? extends Entity>, List<Registry>> registries = new HashMap<>();

    private static int orderIndex;
    private static Set<Class<?>> visited;
    private static MethodHandles.Lookup lookup;

    public static void init() {
        orderIndex = 0;
        visited = new HashSet<>();
        lookup = MethodHandles.lookup();
        registerBuiltIn();
        orderIndex = -1;
        visited = null;
        lookup = null;
    }

    private record Registry(int order, MethodHandle creator) {
        public static final Comparator<Registry> CMP = Comparator.comparingInt(Registry::order);

        public EntityPropertyWidget<?> create(EntityProperties<?> properties) {
            try {
                return (EntityPropertyWidget<?>) creator.invoke(properties);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static List<EntityPropertyWidget<?>> getWidgets(EntityProperties<?> properties) {
        List<Registry> registries = new ArrayList<>(16);
        for (var clazz : EntityUtils.topDown(properties.entity())) {
            registries.addAll(getRegistries(clazz));
        }
        registries.sort(Registry.CMP);

        List<EntityPropertyWidget<?>> res = new ArrayList<>(registries.size());
        for (var registry : registries) {
            res.add(registry.create(properties));
        }
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
            if (!widgetClazz.getSimpleName().startsWith(entityClazz.getSimpleName())
                    && Misc.cast(widgetClazz) != TurnPageTriggerWidget.class) {
                throw new AssertionError(widgetClazz + " must be started with \"" + entityClazz.getSimpleName() + "\"!");
            }

            // Get the constructor of the widget class.
            Constructor<? extends EntityPropertyWidget<E>> constructor = widgetClazz.getDeclaredConstructor(EntityProperties.class);
            createWidget = lookup.unreflectConstructor(constructor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register " + widgetClazz, e);
        }

        // Register it.
        Registry registry = new Registry(++orderIndex, createWidget);
        registries.computeIfAbsent(entityClazz, clazz -> new ArrayList<>()).add(registry);
    }

    private static List<Registry> getRegistries(Class<? extends Entity> clazz) {
        return registries.getOrDefault(clazz, Collections.emptyList());
    }

    private static void registerBuiltIn() {
        r(EntityDisplayWidget.class);
        r(LivingEntityHealthWidget.class);
        r(EntityAirWidget.class);
        r(LivingEntityActiveEffectsWidget.class);
        r(AnimalFoodWidget.class);
        r(MobTemptWidget.class);
        r(EntityLeashableWidget.class);
        r(EntityBoundingBoxWidget.class);
        r(TurnPageTriggerWidget.class);
        r(MobAiWidget.class);
        r(EntityInvulnerableWidget.class);
        r(EntitySoundWidget.class);
        r(AgeableMobAgeWidget.class);
        r(AnimalInLoveWidget.class);
        r(EntityPortalCooldownWidget.class);
    }
}