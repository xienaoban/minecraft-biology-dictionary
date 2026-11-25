package io.github.xienaoban.biologydictionary.core.widget;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.api.plugin.EntityPropertyWidgetsPlugin;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.widget.branch.*;
import io.github.xienaoban.biologydictionary.core.widget.leaf.*;
import io.github.xienaoban.biologydictionary.core.widget.variant.*;
import io.github.xienaoban.biologydictionary.gui.component.property.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.PluginLookup;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.world.entity.Entity;

import java.util.*;

@ClientOnly
public final class EntityPropertyWidgets {

    public static void registerBuiltIn(EntityPropertyWidgetsPlugin.Registrar registrar) {
        registrar.register(EntityDescriptionWidget.class, EntityDescriptionWidget.FACTORY);
        registrar.register(EntityDisplayWidget.class, EntityDisplayWidget.FACTORY);
        registrar.register(LivingEntityHealthWidget.class, LivingEntityHealthWidget.FACTORY);
        registrar.register(EntityAirWidget.class, EntityAirWidget.FACTORY);
        registrar.register(LivingEntityAttackDamageWidget.class, LivingEntityAttackDamageWidget.FACTORY);
        registrar.register(LivingEntityArmorWidget.class, LivingEntityArmorWidget.FACTORY);
        registrar.register(DolphinMoistnessWidget.class, DolphinMoistnessWidget.FACTORY);
        registrar.register(LivingEntityActiveEffectsWidget.class, LivingEntityActiveEffectsWidget.FACTORY);
        registrar.register(AnimalFoodWidget.class, AnimalFoodWidget.FACTORY);
        registrar.register(MobTemptWidget.class, MobTemptWidget.FACTORY);
        registrar.register(LivingEntityLootTableWidget.class, LivingEntityLootTableWidget.FACTORY);
        registrar.register(MobSpawnWidget.class, MobSpawnWidget.FACTORY);
        registrar.register(SlimeSurfaceSpawnWidget.class, SlimeSurfaceSpawnWidget.FACTORY);
        registrar.replace(MobSpawnWidget.class, SlimeSurfaceSpawnWidget.class);
        registrar.register(LivingEntityMovementSpeedWidget.class, LivingEntityMovementSpeedWidget.FACTORY);
        registrar.register(HorseMovementSpeedWidget.class, HorseMovementSpeedWidget.FACTORY);
        registrar.replace(LivingEntityMovementSpeedWidget.class, HorseMovementSpeedWidget.class);
        registrar.register(ZombieHorseMovementSpeedWidget.class, ZombieHorseMovementSpeedWidget.FACTORY);
        registrar.replace(LivingEntityMovementSpeedWidget.class, ZombieHorseMovementSpeedWidget.class);
        registrar.register(LivingEntityJumpStrengthWidget.class, LivingEntityJumpStrengthWidget.FACTORY);
        registrar.register(HorseJumpStrengthWidget.class, HorseJumpStrengthWidget.FACTORY);
        registrar.replace(LivingEntityJumpStrengthWidget.class, HorseJumpStrengthWidget.class);
        registrar.register(SkeletonHorseJumpStrengthWidget.class, SkeletonHorseJumpStrengthWidget.FACTORY);
        registrar.replace(LivingEntityJumpStrengthWidget.class, SkeletonHorseJumpStrengthWidget.class);
        registrar.register(ZombieHorseJumpStrengthWidget.class, ZombieHorseJumpStrengthWidget.FACTORY);
        registrar.replace(LivingEntityJumpStrengthWidget.class, ZombieHorseJumpStrengthWidget.class);
        registrar.register(EntityLeashableWidget.class, EntityLeashableWidget.FACTORY);
        registrar.register(GoatScreamingWidget.class, GoatScreamingWidget.FACTORY);
        registrar.register(EntitySpawnCountedWidget.class, EntitySpawnCountedWidget.FACTORY);
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
    private static final Map<Class<? extends EntityPropertyWidget<?>>, Set<Class<? extends Entity>>>
            exclusions = new HashMap<>();
    private static final Map<Class<? extends EntityPropertyWidget<?>>, Set<Class<? extends EntityPropertyWidget<?>>>>
            replacements = new HashMap<>();

    public static void init() {
        EntityPropertyWidgetsPlugin.Registrar registrar = new EntityPropertyWidgetsPlugin.Registrar() {
            private int orderIndex = 0;
            private final Set<Class<?>> visited = new HashSet<>();

            @Override
            public <E extends Entity> void register(Class<? extends EntityPropertyWidget<E>> widgetClazz,
                    EntityPropertyWidget.Factory<E> widgetFactory) {
                if (!visited.add(widgetClazz)) {
                    throw new IllegalStateException(widgetClazz + " is already registered!");
                }
                Class<?> tmp = Misc.getClazzGeneric(widgetClazz, EntityPropertyWidget.class, 0);
                final Class<E> entityClazz = Misc.cast(tmp.asSubclass(Entity.class));
                Entry entry = new Entry(++orderIndex, widgetClazz, widgetFactory);
                registry.computeIfAbsent(entityClazz, clazz -> new ArrayList<>()).add(entry);
            }

            @Override
            public void exclude(Class<? extends EntityPropertyWidget<?>> widgetClazz,
                    Class<? extends Entity> entityClazz) {
                exclusions.computeIfAbsent(widgetClazz, k -> new HashSet<>()).add(entityClazz);
            }

            @Override
            public void replace(Class<? extends EntityPropertyWidget<?>> oldWidgetClazz,
                    Class<? extends EntityPropertyWidget<?>> newWidgetClazz) {
                replacements.computeIfAbsent(oldWidgetClazz, k -> new HashSet<>()).add(newWidgetClazz);
            }
        };
        registerBuiltIn(registrar);
        for (EntityPropertyWidgetsPlugin plugin : PluginLookup.findClient(EntityPropertyWidgetsPlugin.class)) {
            try {
                plugin.registerEntityPropertyWidgets(registrar);
            } catch (RuntimeException e) {
                throw new IllegalStateException("Failed to register widgets from plugin "
                        + plugin.getClass().getName(), e);
            }
        }
        validateRules();
    }

    private record Entry(int order, Class<? extends EntityPropertyWidget<?>> clazz,
                         EntityPropertyWidget.Factory<?> factory) {
        public static final Comparator<Entry> CMP = Comparator.comparingInt(Entry::order);

        public EntityPropertyWidget<?> create(EntityProperties<?> properties) {
            return factory.create(Misc.cast(properties));
        }
    }

    public static List<EntityPropertyWidget<?>> getWidgets(EntityProperties<?> properties) {
        Entity entity = properties.entity();
        List<Entry> entries = new ArrayList<>(16);
        for (Class<? extends Entity> clazz : EntityUtils.topDown(entity)) {
            entries.addAll(getEntries(clazz));
        }
        entries.sort(Entry.CMP);

        Map<Class<?>, EntityPropertyWidget<?>> present = new LinkedHashMap<>(entries.size());
        for (Entry entry : entries) {
            if (isExcluded(entry, entity)) { continue; }
            EntityPropertyWidget<?> widget = create(entry, properties);
            if (widget != null) { present.put(entry.clazz(), widget); }
        }

        List<EntityPropertyWidget<?>> res = new ArrayList<>(present.size());
        for (Map.Entry<Class<?>, EntityPropertyWidget<?>> created : present.entrySet()) {
            if (isReplaced(created.getKey(), present.keySet())) { continue; }
            res.add(created.getValue());
        }
        return res;
    }

    private static boolean isExcluded(Entry entry, Entity entity) {
        Set<Class<? extends Entity>> excludedEntityClazzes = exclusions.get(entry.clazz());
        if (excludedEntityClazzes == null) { return false; }
        for (Class<? extends Entity> entityClazz : excludedEntityClazzes) {
            if (entityClazz.isInstance(entity)) { return true; }
        }
        return false;
    }

    private static boolean isReplaced(Class<?> widgetClazz, Set<Class<?>> presentWidgetClazzes) {
        Set<Class<? extends EntityPropertyWidget<?>>> replacingWidgetClazzes = replacements.get(widgetClazz);
        if (replacingWidgetClazzes == null) { return false; }
        for (Class<? extends EntityPropertyWidget<?>> replacingWidgetClazz : replacingWidgetClazzes) {
            if (presentWidgetClazzes.contains(replacingWidgetClazz)) { return true; }
        }
        return false;
    }

    private static EntityPropertyWidget<?> create(Entry entry, EntityProperties<?> properties) {
        try {
            return entry.create(properties);
        } catch (Throwable e) {
            BiologyDictionaryClient.printThrowableToLoggerAndGame(
                    "Failed to create entity property widget \"" + entry.clazz().getName()
                            + "\" for entity \"" + EntityUtils.getEntityTypeIdName(properties.entity()) + "\"",
                    e);
            return null;
        }
    }

    private static void validateRules() {
        for (Class<? extends EntityPropertyWidget<?>> widgetClazz : exclusions.keySet()) {
            Class<? extends Entity> registered = requireRegisteredEntityClazz(widgetClazz);
            for (Class<? extends Entity> entityClazz : exclusions.get(widgetClazz)) {
                if (!isRelated(registered, entityClazz)) {
                    throw new IllegalStateException("Excluding " + widgetClazz.getName() + " for "
                            + entityClazz.getName() + " never matches, because the widget is registered for "
                            + registered.getName());
                }
            }
        }
        for (Class<? extends EntityPropertyWidget<?>> oldWidgetClazz : replacements.keySet()) {
            Class<? extends Entity> superseded = requireRegisteredEntityClazz(oldWidgetClazz);
            for (Class<? extends EntityPropertyWidget<?>> newWidgetClazz : replacements.get(oldWidgetClazz)) {
                if (oldWidgetClazz == newWidgetClazz) {
                    throw new IllegalStateException("Widget " + oldWidgetClazz.getName() + " cannot replace itself");
                }
                Class<? extends Entity> superseding = requireRegisteredEntityClazz(newWidgetClazz);
                if (!isRelated(superseded, superseding)) {
                    throw new IllegalStateException("Replacing " + oldWidgetClazz.getName() + " with "
                            + newWidgetClazz.getName() + " never happens, because they are registered for "
                            + "unrelated entity classes " + superseded.getName() + " and " + superseding.getName());
                }
            }
        }
    }

    private static Class<? extends Entity> requireRegisteredEntityClazz(
            Class<? extends EntityPropertyWidget<?>> widgetClazz) {
        for (Map.Entry<Class<? extends Entity>, List<Entry>> registered : registry.entrySet()) {
            for (Entry entry : registered.getValue()) {
                if (entry.clazz() == widgetClazz) {
                    return registered.getKey();
                }
            }
        }
        throw new IllegalStateException(
                "Widget " + widgetClazz.getName() + " must be registered before rules mention it");
    }

    /**
     * Two widgets can coexist on a single entity only if one of their entity classes extends the other,
     * since entities inherit from a single class chain.
     */
    private static boolean isRelated(Class<?> a, Class<?> b) {
        return a.isAssignableFrom(b) || b.isAssignableFrom(a);
    }

    private static List<Entry> getEntries(Class<? extends Entity> clazz) {
        return registry.getOrDefault(clazz, Collections.emptyList());
    }
}
