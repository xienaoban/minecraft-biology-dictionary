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

import java.util.*;

@Environment(EnvType.CLIENT)
public final class EntityPropertyWidgets {

    private static final Map<Class<? extends Entity>, List<Entry>> registry = new HashMap<>();

    private static int orderIndex;
    private static Set<Class<?>> visited;

    private static void registerBuiltIn() {
        r(EntityDisplayWidget.class, EntityDisplayWidget.FACTORY);
        r(LivingEntityHealthWidget.class, LivingEntityHealthWidget.FACTORY);
        r(EntityAirWidget.class, EntityAirWidget.FACTORY);
        r(DolphinMoistnessWidget.class, DolphinMoistnessWidget.FACTORY);
        r(LivingEntityActiveEffectsWidget.class, LivingEntityActiveEffectsWidget.FACTORY);
        r(AnimalFoodWidget.class, AnimalFoodWidget.FACTORY);
        r(MobTemptWidget.class, MobTemptWidget.FACTORY);
        r(LivingEntityMovementSpeedWidget.class, LivingEntityMovementSpeedWidget.FACTORY);
        r(LivingEntityJumpStrengthWidget.class, LivingEntityJumpStrengthWidget.FACTORY);
        r(EntityLeashableWidget.class, EntityLeashableWidget.FACTORY);
        r(GoatScreamingWidget.class, GoatScreamingWidget.FACTORY);
        r(EntityBoundingBoxWidget.class, EntityBoundingBoxWidget.FACTORY);
        r(TurnPageTriggerWidget.class, TurnPageTriggerWidget.FACTORY);
        r(EntityStandardVariantWidget.class, EntityStandardVariantWidget.FACTORY);
        r(HorseMarkingsWidget.class, HorseMarkingsWidget.FACTORY);
        r(PandaMainGeneWidget.class, PandaMainGeneWidget.FACTORY);
        r(PandaHiddenGeneWidget.class, PandaHiddenGeneWidget.FACTORY);
        r(VillagerTypeWidget.class, VillagerTypeWidget.FACTORY);
        r(VillagerScheduleWidget.class, VillagerScheduleWidget.FACTORY);
        r(MobAiWidget.class, MobAiWidget.FACTORY);
        r(EntityInvulnerableWidget.class, EntityInvulnerableWidget.FACTORY);
        r(EntitySoundWidget.class, EntitySoundWidget.FACTORY);
        r(MobPersistenceWidget.class, MobPersistenceWidget.FACTORY);
        r(LivingEntityInventoryWidget.class, LivingEntityInventoryWidget.FACTORY);
        r(SheepEatGrassWidget.class, SheepEatGrassWidget.FACTORY);
        r(AgeableMobGrowthWidget.class, AgeableMobGrowthWidget.FACTORY);
        r(AgeableMobBreedingCooldownWidget.class, AgeableMobBreedingCooldownWidget.FACTORY);
        r(AnimalInLoveWidget.class, AnimalInLoveWidget.FACTORY);
        r(EntityPortalCooldownWidget.class, EntityPortalCooldownWidget.FACTORY);
        r(EntityOwnerWidget.class, EntityOwnerWidget.FACTORY);
        r(VillagerJobSiteWidget.class, VillagerJobSiteWidget.FACTORY);
        r(VillagerRestocksTodayWidget.class, VillagerRestocksTodayWidget.FACTORY);
        r(BeeHivePropertyWidget.class, BeeHivePropertyWidget.FACTORY);
        r(WanderingTraderDespawnDelayWidget.class, WanderingTraderDespawnDelayWidget.FACTORY);
    }

    public static void init() {
        orderIndex = 0;
        visited = new HashSet<>();
        registerBuiltIn();
        orderIndex = -1;
        visited = null;
    }

    private record Entry(int order, Class<? extends EntityPropertyWidget<?>> clazz,
                         EntityPropertyWidget.Factory<?> factory) {
        public static final Comparator<Entry> CMP = Comparator.comparingInt(Entry::order);

        public EntityPropertyWidget<?> create(EntityProperties<?> properties) {
            return factory.create(Misc.cast(properties));
        }
    }

    public static List<EntityPropertyWidget<?>> getWidgets(EntityProperties<?> properties) {
        List<Entry> entries = new ArrayList<>(16);
        for (Class<? extends Entity> clazz : EntityUtils.topDown(properties.entity())) {
            entries.addAll(getEntries(clazz));
        }
        entries.sort(Entry.CMP);

        List<EntityPropertyWidget<?>> res = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            EntityPropertyWidget<?> widget = entry.create(properties);
            if (widget != null) {
                res.add(entry.create(properties));
            }
        }
        return res;
    }

    private static <E extends Entity> void r(Class<? extends EntityPropertyWidget<E>> widgetClazz,
                                             EntityPropertyWidget.Factory<?> widgetFactory) {
        if (!visited.add(widgetClazz)) {
            throw new IllegalStateException(widgetClazz + " is already registered!");
        }

        Class<?> tmp = Misc.getClazzGeneric(widgetClazz, EntityPropertyWidget.class, 0);
        final Class<E> entityClazz = Misc.cast(tmp.asSubclass(Entity.class));
        if (!widgetClazz.getSimpleName().startsWith(entityClazz.getSimpleName())
                && Misc.cast(widgetClazz) != TurnPageTriggerWidget.class) {
            throw new AssertionError(widgetClazz + " must be started with \""
                    + entityClazz.getSimpleName() + "\"!");
        }

        // Register it.
        Entry entry = new Entry(++orderIndex, widgetClazz, widgetFactory);
        EntityPropertyWidgets.registry.computeIfAbsent(entityClazz,
                clazz -> new ArrayList<>()).add(entry);
    }

    private static List<Entry> getEntries(Class<? extends Entity> clazz) {
        return registry.getOrDefault(clazz, Collections.emptyList());
    }
}