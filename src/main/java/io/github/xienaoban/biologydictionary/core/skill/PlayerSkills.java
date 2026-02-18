package io.github.xienaoban.biologydictionary.core.skill;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.skill.entity.*;
import io.github.xienaoban.biologydictionary.core.skill.general.GetSpawnEggSkill;
import io.github.xienaoban.biologydictionary.core.skill.general.HighlightEntitiesSkill;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public final class PlayerSkills {

    public static void registerBuiltIn(Registrar registrar) {
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
        registrar.register(AgeableMobSetForcedAgeSkill.class, AgeableMobSetForcedAgeSkill.META);
        registrar.register(BeeClearHiveSkill.class, BeeClearHiveSkill.META);
        registrar.register(EntityGiftPetSkill.class, EntityGiftPetSkill.META);
        registrar.register(VillagerForceRestockSkill.class, VillagerForceRestockSkill.META);
        registrar.register(WanderingTraderRetainSkill.class, WanderingTraderRetainSkill.META);
    }

    private static final Map<String, GeneralSkill.Meta<?>> commonSkills = new HashMap<>();
    private static final Map<String, EntityTargetedSkill.Meta<?>> entityTargetedSkills = new HashMap<>();

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
    }

    private static <T extends EntityTargetedSkill<?>> void register0(Class<T> skillClass, EntityTargetedSkill.Meta<T> meta) {
        if (entityTargetedSkills.putIfAbsent(key(skillClass), meta) != null) {
            throw new RuntimeException("Duplicate skill registered: " + key(skillClass));
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

    /**
     * @deprecated Use {@link #getCommonSkillMeta(String)} instead
     */
    @Deprecated
    public static GeneralSkill.Meta<?> getCommonSkillFactory(String key) {
        return getCommonSkillMeta(key);
    }

    /**
     * @deprecated Use {@link #getEntityTargetedSkillMeta(String)} instead
     */
    @Deprecated
    public static EntityTargetedSkill.Meta<?> getEntityTargetedSkillFactory(String key) {
        return getEntityTargetedSkillMeta(key);
    }

    public static String key(Object skill) {
        return skill.getClass().getName();
    }

    public static String key(Class<?> skillClass) {
        return skillClass.getName();
    }

    public static void giveExperiencePointsIfNotCreative(ServerPlayer player, int experience) {
        if (PlayerUtils.isCreative(player)) { return; }
        PlayerUtils.giveExperiencePoints(player, experience);
        PlayerUtils.playLocalSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 0.01F);
    }

    public static void giveExperienceLevelsIfNotCreative(ServerPlayer player, int experience) {
        if (PlayerUtils.isCreative(player)) { return; }
        PlayerUtils.giveExperienceLevels(player, experience);
        PlayerUtils.playLocalSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 0.01F);
    }

    @Environment(EnvType.CLIENT)
    public static boolean activate(GeneralSkill skill) {
        try {
            skill.clientAdditionalCheck(ClientUtils.getClientPlayer());
            ClientNetManager.sendCommonSkill(skill);
            return true;
        } catch (NoPermissionException e) {
            BiologyDictionaryClient.sendCenteredWarning(e.getGameMessage());
        } catch (Exception e) {
            LOGGER.warn(Misc.getStackToString(e));
        }
        return false;
    }

    @Environment(EnvType.CLIENT)
    public static boolean activate(Entity entity, EntityTargetedSkill<?> skill) {
        try {
            skill.clientAdditionalCheck(ClientUtils.getClientPlayer(), Misc.cast(entity));
            ClientNetManager.sendEntityTargetedSkill(entity, skill);
            return true;
        } catch (NoPermissionException e) {
            BiologyDictionaryClient.sendCenteredWarning(e.getGameMessage());
        } catch (Exception e) {
            LOGGER.warn(Misc.getStackToString(e));
        }
        return false;
    }

    public interface Registrar {
        <T extends GeneralSkill> void register(Class<T> skillClass, GeneralSkill.Meta<T> meta);
        <T extends EntityTargetedSkill<?>> void register(Class<T> skillClass, EntityTargetedSkill.Meta<T> meta);
    }
}
