package io.github.xienaoban.biologydictionary.core.skill;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.api.plugin.BiologySkillsPlugin;
import io.github.xienaoban.biologydictionary.core.skill.entity.*;
import io.github.xienaoban.biologydictionary.core.skill.general.GetSpawnEggSkill;
import io.github.xienaoban.biologydictionary.core.skill.general.HighlightEntitiesSkill;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.PluginLookup;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BiologySkills {

    public static void registerBuiltIn(BiologySkillsPlugin.Registrar registrar) {
        registrar.register(HighlightEntitiesSkill.class, HighlightEntitiesSkill.META);
        registrar.register(GetSpawnEggSkill.class, GetSpawnEggSkill.META);

        registrar.register(EntitySetVariantSkill.class, EntitySetVariantSkill.META);
        registrar.register(EntitySetInvulnerableSkill.class, EntitySetInvulnerableSkill.META);
        registrar.register(EntitySetSoundSkill.class, EntitySetSoundSkill.META);
        registrar.register(EntitySetPortalCooldownSkill.class, EntitySetPortalCooldownSkill.META);
        registrar.register(MobSetNoAiSkill.class, MobSetNoAiSkill.META);
        registrar.register(MobForcePersistentSkill.class, MobForcePersistentSkill.META);
        registrar.register(LivingEntityStealInventorySkill.class, LivingEntityStealInventorySkill.META);
        registrar.register(SheepForceEatGrassSkill.class, SheepForceEatGrassSkill.META);
        registrar.register(AgeableMobSetBreedingCooldownSkill.class, AgeableMobSetBreedingCooldownSkill.META);
        registrar.register(AgeableMobSetAgeLockedSkill.class, AgeableMobSetAgeLockedSkill.META);
        registrar.register(TadpoleSetAgeLockedSkill.class, TadpoleSetAgeLockedSkill.META);
        registrar.register(BeeClearHiveSkill.class, BeeClearHiveSkill.META);
        registrar.register(EntityGiftPetSkill.class, EntityGiftPetSkill.META);
        registrar.register(VillagerForceRestockSkill.class, VillagerForceRestockSkill.META);
        registrar.register(WanderingTraderRetainSkill.class, WanderingTraderRetainSkill.META);
    }

    private static final Map<String, GeneralSkill.Meta<?>> commonSkills = new LinkedHashMap<>();
    private static final Map<String, EntityTargetedSkill.Meta<?>> entityTargetedSkills = new LinkedHashMap<>();
    private static final Map<String, Class<?>> skillClasses = new LinkedHashMap<>();

    public static void init() {
        BiologySkillsPlugin.Registrar registrar = new BiologySkillsPlugin.Registrar() {
            @Override
            public <T extends GeneralSkill> void register(Class<T> skillClass, GeneralSkill.Meta<T> meta) {
                if (commonSkills.putIfAbsent(key(skillClass), meta) != null) {
                    throw new RuntimeException("Duplicate skill registered: " + key(skillClass));
                }
                if (skillClasses.putIfAbsent(meta.shortName(), skillClass) != null) {
                    throw new RuntimeException("Duplicate short name: " + meta.shortName());
                }
            }

            @Override
            public <T extends EntityTargetedSkill<?>> void register(Class<T> skillClass,
                    EntityTargetedSkill.Meta<T> meta) {
                if (entityTargetedSkills.putIfAbsent(key(skillClass), meta) != null) {
                    throw new RuntimeException("Duplicate skill registered: " + key(skillClass));
                }
                if (skillClasses.putIfAbsent(meta.shortName(), skillClass) != null) {
                    throw new RuntimeException("Duplicate short name: " + meta.shortName());
                }
            }
        };

        registerBuiltIn(registrar);
        for (BiologySkillsPlugin plugin : PluginLookup.find(BiologySkillsPlugin.class)) {
            try {
                plugin.registerBiologySkills(registrar);
            } catch (RuntimeException e) {
                throw new IllegalStateException("Failed to register skills from plugin "
                        + plugin.getClass().getName(), e);
            }
        }
    }

    public static GeneralSkill.Meta<?> getCommonSkillMeta(String key) {
        GeneralSkill.Meta<?> res = commonSkills.get(key);
        if (res == null) {
            throw new RuntimeException("No such key: " + key);
        }
        return res;
    }

    public static EntityTargetedSkill.Meta<?> getEntityTargetedSkillMeta(String key) {
        EntityTargetedSkill.Meta<?> res = entityTargetedSkills.get(key);
        if (res == null) {
            throw new RuntimeException("No such key: " + key);
        }
        return res;
    }

    public static Class<?> getSkillClass(String shortName) {
        Class<?> res = skillClasses.get(shortName);
        if (res == null) {
            throw new RuntimeException("No such short name: " + shortName);
        }
        return res;
    }

    /**
     * Read-only snapshot of all registered common-skill metas. Only valid after {@link #init()}.
     */
    public static Collection<GeneralSkill.Meta<?>> commonSkillMetas() {
        return Collections.unmodifiableCollection(commonSkills.values());
    }

    /**
     * Read-only snapshot of all registered entity-targeted-skill metas. Only valid after {@link #init()}.
     */
    public static Collection<EntityTargetedSkill.Meta<?>> entityTargetedSkillMetas() {
        return Collections.unmodifiableCollection(entityTargetedSkills.values());
    }

    public static String key(Object skill) {
        return skill.getClass().getName();
    }

    public static String key(Class<?> skillClass) {
        return skillClass.getName();
    }

    @ClientOnly
    public static boolean activate(GeneralSkill skill) {
        @ClientOnly final class CO { static boolean activate(GeneralSkill skill) {
            try {
                LocalPlayer player = ClientUtils.getClientPlayer();
                skill.clientAdditionalCheck(new GeneralSkill.ClientContext(player));
                SkillCost cost = skill.getRealCost();
                cost.clientCheck(new SkillCost.ClientContext(player));
                ClientNetManager.sendCommonSkill(skill);
                return true;
            } catch (NoPermissionException e) {
                BiologyDictionaryClient.sendCenteredWarning(e.getGameMessage());
            } catch (Exception e) {
                BiologyDictionaryClient.printThrowableToLoggerAndGame(
                        "Failed to activate skill \"" + skill.getClass() + "\"", e);
            }
            return false;
        }}
        return CO.activate(skill);
    }

    @ClientOnly
    public static boolean activate(Entity entity, EntityTargetedSkill<?> skill) {
        @ClientOnly final class CO { static boolean activate(Entity entity, EntityTargetedSkill<?> skill) {
            try {
                if (EntityUtils.isFakeEntity(entity)) {
                    // Entity displayed in overview screen, not a real entity in world.
                    return true;
                }
                LocalPlayer player = ClientUtils.getClientPlayer();
                skill.clientAdditionalCheck(new EntityTargetedSkill.ClientContext<>(player, Misc.cast(entity)));
                SkillCost cost = skill.getRealCost(Misc.cast(entity));
                cost.clientCheck(new SkillCost.ClientContext(player));
                ClientNetManager.sendEntityTargetedSkill(entity, skill);
                return true;
            } catch (NoPermissionException e) {
                BiologyDictionaryClient.sendCenteredWarning(e.getGameMessage());
            } catch (Exception e) {
                BiologyDictionaryClient.printThrowableToLoggerAndGame(
                        "Failed to activate skill \"" + skill.getClass()
                                + "\" of entity \"" + EntityUtils.getEntityTypeIdName(entity) + "\"", e);
            }
            return false;
        }}
        return CO.activate(entity, skill);
    }
}
