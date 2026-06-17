package io.github.xienaoban.biologydictionary.core.skill;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.core.skill.entity.EntitySetInvulnerableSkill;
import io.github.xienaoban.biologydictionary.core.skill.entity.EntitySetPortalCooldownSkill;
import io.github.xienaoban.biologydictionary.core.skill.entity.EntitySetSoundSkill;
import io.github.xienaoban.biologydictionary.core.skill.entity.EntitySetVariantSkill;
import io.github.xienaoban.biologydictionary.core.skill.entity.AgeableMobSetBreedingCooldownSkill;
import io.github.xienaoban.biologydictionary.core.skill.entity.AgeableMobSetAgeLockedSkill;
import io.github.xienaoban.biologydictionary.core.skill.entity.BeeClearHiveSkill;
import io.github.xienaoban.biologydictionary.core.skill.entity.EntityGiftPetSkill;
import io.github.xienaoban.biologydictionary.core.skill.entity.LivingEntityStealInventorySkill;
import io.github.xienaoban.biologydictionary.core.skill.entity.MobForcePersistentSkill;
import io.github.xienaoban.biologydictionary.core.skill.entity.MobSetNoAiSkill;
import io.github.xienaoban.biologydictionary.core.skill.entity.SheepForceEatGrassSkill;
import io.github.xienaoban.biologydictionary.core.skill.entity.TadpoleSetAgeLockedSkill;
import io.github.xienaoban.biologydictionary.core.skill.entity.VillagerForceRestockSkill;
import io.github.xienaoban.biologydictionary.core.skill.entity.WanderingTraderRetainSkill;
import io.github.xienaoban.biologydictionary.core.skill.general.GetSpawnEggSkill;
import io.github.xienaoban.biologydictionary.core.skill.general.HighlightEntitiesSkill;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public final class BiologySkills {
    private static final Map<String, GeneralSkill.Meta<?>> commonSkills = new HashMap<>();
    private static final Map<String, EntityTargetedSkill.Meta<?>> entityTargetedSkills = new HashMap<>();
    private static final Map<String, Class<?>> skillClasses = new HashMap<>();

    private BiologySkills() {}

    public static void registerBuiltIn(Registrar registrar) {
        registrar.register(EntitySetVariantSkill.class, EntitySetVariantSkill.META);
        registrar.register(EntitySetInvulnerableSkill.class, EntitySetInvulnerableSkill.META);
        registrar.register(EntitySetSoundSkill.class, EntitySetSoundSkill.META);
        registrar.register(EntitySetPortalCooldownSkill.class, EntitySetPortalCooldownSkill.META);
        registrar.register(MobSetNoAiSkill.class, MobSetNoAiSkill.META);
        registrar.register(MobForcePersistentSkill.class, MobForcePersistentSkill.META);
        registrar.register(AgeableMobSetBreedingCooldownSkill.class, AgeableMobSetBreedingCooldownSkill.META);
        registrar.register(AgeableMobSetAgeLockedSkill.class, AgeableMobSetAgeLockedSkill.META);
        registrar.register(TadpoleSetAgeLockedSkill.class, TadpoleSetAgeLockedSkill.META);
        registrar.register(BeeClearHiveSkill.class, BeeClearHiveSkill.META);
        registrar.register(EntityGiftPetSkill.class, EntityGiftPetSkill.META);
        registrar.register(SheepForceEatGrassSkill.class, SheepForceEatGrassSkill.META);
        registrar.register(VillagerForceRestockSkill.class, VillagerForceRestockSkill.META);
        registrar.register(WanderingTraderRetainSkill.class, WanderingTraderRetainSkill.META);
        registrar.register(GetSpawnEggSkill.class, GetSpawnEggSkill.META);
        registrar.register(HighlightEntitiesSkill.class, HighlightEntitiesSkill.META);
        registrar.register(LivingEntityStealInventorySkill.class, LivingEntityStealInventorySkill.META);
    }

    public static void init() {
        Registrar registrar = new Registrar() {
            @Override
            public <T extends GeneralSkill> void register(Class<T> skillClass, GeneralSkill.Meta<T> meta) {
                register0(skillClass, meta);
            }

            @Override
            public <T extends EntityTargetedSkill<?>> void register(Class<T> skillClass, EntityTargetedSkill.Meta<T> meta) {
                register0(skillClass, meta);
            }
        };
        registerBuiltIn(registrar);
    }

    private static <T extends GeneralSkill> void register0(Class<T> skillClass, GeneralSkill.Meta<T> meta) {
        if (commonSkills.putIfAbsent(key(skillClass), meta) != null) {
            throw new RuntimeException("Duplicate skill registered: " + key(skillClass));
        }
        if (skillClasses.putIfAbsent(meta.shortName(), skillClass) != null) {
            throw new RuntimeException("Duplicate short name: " + meta.shortName());
        }
    }

    private static <T extends EntityTargetedSkill<?>> void register0(Class<T> skillClass, EntityTargetedSkill.Meta<T> meta) {
        if (entityTargetedSkills.putIfAbsent(key(skillClass), meta) != null) {
            throw new RuntimeException("Duplicate skill registered: " + key(skillClass));
        }
        if (skillClasses.putIfAbsent(meta.shortName(), skillClass) != null) {
            throw new RuntimeException("Duplicate short name: " + meta.shortName());
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
                        "Failed to activate skill \"" + skill.getClass() + "\" of entity \""
                                + EntityUtils.getEntityTypeIdName(entity) + "\"", e);
            }
            return false;
        }}
        return CO.activate(entity, skill);
    }

    public interface Registrar {
        <T extends GeneralSkill> void register(Class<T> skillClass, GeneralSkill.Meta<T> meta);
        <T extends EntityTargetedSkill<?>> void register(Class<T> skillClass, EntityTargetedSkill.Meta<T> meta);
    }
}
