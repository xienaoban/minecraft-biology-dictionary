package io.github.xienaoban.biologydictionary.core.widget;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.widget.branch.*;
import io.github.xienaoban.biologydictionary.core.widget.leaf.*;
import io.github.xienaoban.biologydictionary.core.widget.variant.*;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

import java.util.*;

@Environment(EnvType.CLIENT)
public final class EntityPropertyWidgets {

    public static void registerBuiltIn(Registrar registrar) {
        registrar.register(EntityDescriptionWidget.class, EntityDescriptionWidget.FACTORY);
        registrar.register(EntityDisplayWidget.class, EntityDisplayWidget.FACTORY);
        registrar.register(LivingEntityHealthWidget.class, LivingEntityHealthWidget.FACTORY);
        registrar.register(EntityAirWidget.class, EntityAirWidget.FACTORY);
        registrar.register(DolphinMoistnessWidget.class, DolphinMoistnessWidget.FACTORY);
        registrar.register(LivingEntityActiveEffectsWidget.class, LivingEntityActiveEffectsWidget.FACTORY);
        registrar.register(AnimalFoodWidget.class, AnimalFoodWidget.FACTORY);
        registrar.register(MobTemptWidget.class, MobTemptWidget.FACTORY);
        registrar.register(EntityLootTableWidget.class, EntityLootTableWidget.FACTORY);
        registrar.register(MobSpawnWidget.class, MobSpawnWidget.FACTORY);
        registrar.register(LivingEntityMovementSpeedWidget.class, LivingEntityMovementSpeedWidget.FACTORY);
        registrar.register(LivingEntityJumpStrengthWidget.class, LivingEntityJumpStrengthWidget.FACTORY);
        registrar.register(EntityLeashableWidget.class, EntityLeashableWidget.FACTORY);
        registrar.register(GoatScreamingWidget.class, GoatScreamingWidget.FACTORY);
        registrar.register(EntityBoundingBoxWidget.class, EntityBoundingBoxWidget.FACTORY);
        registrar.register(TurnPagePlaceholder.TurnPage1Widget.class, TurnPagePlaceholder.TurnPage1Widget.FACTORY);
        registrar.register(EntityStandardVariantWidget.class, EntityStandardVariantWidget.FACTORY);
        registrar.register(HorseMarkingsWidget.class, HorseMarkingsWidget.FACTORY);
        registrar.register(PandaMainGeneWidget.class, PandaMainGeneWidget.FACTORY);
        registrar.register(PandaHiddenGeneWidget.class, PandaHiddenGeneWidget.FACTORY);
        registrar.register(VillagerTypeWidget.class, VillagerTypeWidget.FACTORY);
        registrar.register(VillagerScheduleWidget.class, VillagerScheduleWidget.FACTORY);
        registrar.register(MobAiWidget.class, MobAiWidget.FACTORY);
        registrar.register(EntityInvulnerableWidget.class, EntityInvulnerableWidget.FACTORY);
        registrar.register(EntitySoundWidget.class, EntitySoundWidget.FACTORY);
        registrar.register(MobPersistenceWidget.class, MobPersistenceWidget.FACTORY);
        registrar.register(SheepEatGrassWidget.class, SheepEatGrassWidget.FACTORY);
        registrar.register(AgeableMobGrowthWidget.class, AgeableMobGrowthWidget.FACTORY);
        registrar.register(AgeableMobBreedingCooldownWidget.class, AgeableMobBreedingCooldownWidget.FACTORY);
        registrar.register(AnimalInLoveWidget.class, AnimalInLoveWidget.FACTORY);
        registrar.register(EntityPortalCooldownWidget.class, EntityPortalCooldownWidget.FACTORY);
        registrar.register(EntityOwnerWidget.class, EntityOwnerWidget.FACTORY);
        registrar.register(LivingEntityInventoryWidget.class, LivingEntityInventoryWidget.FACTORY);
        registrar.register(VillagerJobSiteWidget.class, VillagerJobSiteWidget.FACTORY);
        registrar.register(VillagerRestocksTodayWidget.class, VillagerRestocksTodayWidget.FACTORY);
        registrar.register(BeeHivePropertyWidget.class, BeeHivePropertyWidget.FACTORY);
        registrar.register(WanderingTraderDespawnDelayWidget.class, WanderingTraderDespawnDelayWidget.FACTORY);
        registrar.register(TurnPagePlaceholder.TurnPage2Widget.class, TurnPagePlaceholder.TurnPage2Widget.FACTORY);
        registrar.register(EntityDiscoveryRecordWidget.class, EntityDiscoveryRecordWidget.FACTORY);
    }

    private static final Map<Class<? extends Entity>, List<Entry>> registry = new HashMap<>();

    public static void init() {
        Registrar registrar = new Registrar() {
            private int orderIndex = 0;
            private final Set<Class<?>> visited = new HashSet<>();

            @Override
            public <E extends Entity> void register(Class<? extends EntityPropertyWidget<E>> widgetClazz, EntityPropertyWidget.Factory<E> widgetFactory) {
                register0(widgetClazz, widgetFactory, ++orderIndex, visited);
            }
        };
        registerBuiltIn(registrar);
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

    private static <E extends Entity> void register0(Class<? extends EntityPropertyWidget<E>> widgetClazz,
                                                    EntityPropertyWidget.Factory<?> widgetFactory,
                                                    int orderIndex, Set<Class<?>> visited) {
        if (!visited.add(widgetClazz)) {
            throw new IllegalStateException(widgetClazz + " is already registered!");
        }

        Class<?> tmp = Misc.getClazzGeneric(widgetClazz, EntityPropertyWidget.class, 0);
        final Class<E> entityClazz = Misc.cast(tmp.asSubclass(Entity.class));

        // Register it.
        Entry entry = new Entry(orderIndex, widgetClazz, widgetFactory);
        EntityPropertyWidgets.registry.computeIfAbsent(entityClazz,
                clazz -> new ArrayList<>()).add(entry);
    }

    private static List<Entry> getEntries(Class<? extends Entity> clazz) {
        return registry.getOrDefault(clazz, Collections.emptyList());
    }

    @FunctionalInterface
    public interface Registrar {
        <E extends Entity> void register(Class<? extends EntityPropertyWidget<E>> widgetClazz,
                                         EntityPropertyWidget.Factory<E> widgetFactory);
    }
}
