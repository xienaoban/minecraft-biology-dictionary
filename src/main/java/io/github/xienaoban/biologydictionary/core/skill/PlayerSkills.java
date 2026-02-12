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
        registrar.register(HighlightEntitiesSkill.class, HighlightEntitiesSkill.FACTORY);
        registrar.register(GetSpawnEggSkill.class, GetSpawnEggSkill.FACTORY);

        registrar.register(EntitySetVariantSkill.class, EntitySetVariantSkill.FACTORY);
        registrar.register(EntitySetInvulnerableSkill.class, EntitySetInvulnerableSkill.FACTORY);
        registrar.register(EntitySetSoundSkill.class, EntitySetSoundSkill.FACTORY);
        registrar.register(EntitySetPortalCooldownSkill.class, EntitySetPortalCooldownSkill.FACTORY);
        registrar.register(MobSetNoAiSkill.class, MobSetNoAiSkill.FACTORY);
        registrar.register(MobForcePersistentSkill.class, MobForcePersistentSkill.FACTORY);
        registrar.register(LivingEntityStealInventorySkill.class, LivingEntityStealInventorySkill.FACTORY);
        registrar.register(SheepForceEatGrassSkill.class, SheepForceEatGrassSkill.FACTORY);
        registrar.register(AgeableMobSetForcedAgeSkill.class, AgeableMobSetForcedAgeSkill.FACTORY);
        registrar.register(BeeClearHiveSkill.class, BeeClearHiveSkill.FACTORY);
        registrar.register(EntityGiftPetSkill.class, EntityGiftPetSkill.FACTORY);
        registrar.register(VillagerForceRestockSkill.class, VillagerForceRestockSkill.FACTORY);
        registrar.register(WanderingTraderRetainSkill.class, WanderingTraderRetainSkill.FACTORY);
    }

    private static final Map<String, GeneralSkill.Factory<?>> commonSkills = new HashMap<>();
    private static final Map<String, EntityTargetedSkill.Factory<?>> entityTargetedSkills = new HashMap<>();

    public static void init() {
        Registrar registrar = new Registrar() {
            @Override
            public <T extends GeneralSkill> void register(Class<T> skillClass, GeneralSkill.Factory<T> factory) {
                register0(skillClass, factory);
            }

            @Override
            public <T extends EntityTargetedSkill<?>> void register(Class<T> skillClass, EntityTargetedSkill.Factory<T> factory) {
                register0(skillClass, factory);
            }
        };
        registerBuiltIn(registrar);
    }

    private static <T extends GeneralSkill> void register0(Class<T> skillClass, GeneralSkill.Factory<T> factory) {
        if (commonSkills.putIfAbsent(key(skillClass), factory) != null) {
            throw new RuntimeException("Duplicate skill registered: " + key(skillClass));
        }
    }

    private static <T extends EntityTargetedSkill<?>> void register0(Class<T> skillClass, EntityTargetedSkill.Factory<T> factory) {
        if (entityTargetedSkills.putIfAbsent(key(skillClass), factory) != null) {
            throw new RuntimeException("Duplicate skill registered: " + key(skillClass));
        }
    }

    public static GeneralSkill.Factory<?> getCommonSkillFactory(String key) {
        GeneralSkill.Factory<?> res = commonSkills.get(key);
        if (res == null) {
            throw new RuntimeException("No such key: " + key);
        }
        return res;
    }

    public static EntityTargetedSkill.Factory<?> getEntityTargetedSkillFactory(String key) {
        EntityTargetedSkill.Factory<?> res = entityTargetedSkills.get(key);
        if (res == null) {
            throw new RuntimeException("No such key: " + key);
        }
        return res;
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
            skill.clientCheck(ClientUtils.getClientPlayer());
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
            skill.clientCheck(ClientUtils.getClientPlayer(), Misc.cast(entity));
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
        <T extends GeneralSkill> void register(Class<T> skillClass, GeneralSkill.Factory<T> factory);
        <T extends EntityTargetedSkill<?>> void register(Class<T> skillClass, EntityTargetedSkill.Factory<T> factory);
    }
}
