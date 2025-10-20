package io.github.xienaoban.biologydictionary.core.widget;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.widget.branch.*;
import io.github.xienaoban.biologydictionary.core.widget.leaf.*;
import io.github.xienaoban.biologydictionary.core.widget.variant.*;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
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

    private record Registry(int order, Class<? extends EntityPropertyWidget<?>> clazz, MethodHandle creator) {
        public static final Comparator<Registry> CMP = Comparator.comparingInt(Registry::order);

        public EntityPropertyWidget<?> create(EntityProperties<?> properties) {
            try {
                return (EntityPropertyWidget<?>) creator.invoke(properties);
            } catch (RuntimeException e) {
                throw e;
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
            try {
                res.add(registry.create(properties));
            } catch (UnsupportedWidgetException ignored) {
                // Verification failed. This widget doesn't fit the entity.
            }
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
            entityClazz = Misc.cast(Misc.getClazzGeneric(widgetClazz, EntityPropertyWidget.class, 0)
                    .asSubclass(Entity.class));
            if (!widgetClazz.getSimpleName().startsWith(entityClazz.getSimpleName())
                    && Misc.cast(widgetClazz) != TurnPageTriggerWidget.class) {
                throw new AssertionError(widgetClazz + " must be started with \"" + entityClazz.getSimpleName() + "\"!");
            }

            // Get the constructor of the widget class.
            createWidget = lookup.findConstructor(widgetClazz, MethodType.methodType(void.class, EntityProperties.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to register " + widgetClazz, e);
        }

        // Register it.
        Registry registry = new Registry(++orderIndex, widgetClazz, createWidget);
        registries.computeIfAbsent(entityClazz, clazz -> new ArrayList<>()).add(registry);
    }

    private static List<Registry> getRegistries(Class<? extends Entity> clazz) {
        return registries.getOrDefault(clazz, Collections.emptyList());
    }

    private static void registerBuiltIn() {
        r(EntityDisplayWidget.class);
        r(LivingEntityHealthWidget.class);
        r(EntityAirWidget.class);
        r(DolphinMoistnessWidget.class);
        r(LivingEntityActiveEffectsWidget.class);
        r(AnimalFoodWidget.class);
        r(MobTemptWidget.class);
        r(LivingEntityMovementSpeedWidget.class);
        r(LivingEntityJumpStrengthWidget.class);
        r(EntityLeashableWidget.class);
        r(GoatScreamingWidget.class);
        r(EntityBoundingBoxWidget.class);
        r(TurnPageTriggerWidget.class);
        r(EntityStandardVariantWidget.class);
        r(HorseMarkingsWidget.class);
        r(PandaMainGeneWidget.class);
        r(PandaHiddenGeneWidget.class);
        r(VillagerTypeWidget.class);
        r(VillagerScheduleWidget.class);
        r(MobAiWidget.class);
        r(EntityInvulnerableWidget.class);
        r(EntitySoundWidget.class);
        r(SheepEatGrassWidget.class);
        r(AgeableMobGrowthWidget.class);
        r(AgeableMobBreedingCooldownWidget.class);
        r(AnimalInLoveWidget.class);
        r(EntityPortalCooldownWidget.class);
        r(EntityOwnerWidget.class);
        r(VillagerJobSiteWidget.class);
        r(VillagerRestocksTodayWidget.class);
        r(BeeHivePropertyWidget.class);
        r(WanderingTraderDespawnDelayWidget.class);
    }
}