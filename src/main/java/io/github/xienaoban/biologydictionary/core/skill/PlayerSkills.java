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
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public final class PlayerSkills {

    public static void registerBuiltIn(Registrar registrar) {
        registrar.register(new HighlightEntitiesSkill());
        registrar.register(new GetSpawnEggSkill());

        registrar.register(new EntitySetVariantSkill());
        registrar.register(new EntitySetInvulnerableSkill());
        registrar.register(new EntitySetSoundSkill());
        registrar.register(new EntitySetPortalCooldownSkill());
        registrar.register(new MobSetNoAiSkill());
        registrar.register(new MobForcePersistentSkill());
        registrar.register(new LivingEntityStealInventorySkill());
        registrar.register(new SheepForceEatGrassSkill());
        registrar.register(new AgeableMobSetForcedAgeSkill());
        registrar.register(new BeeClearHiveSkill());
        registrar.register(new EntityGiftPetSkill());
        registrar.register(new VillagerForceRestockSkill());
        registrar.register(new WanderingTraderRetainSkill());
    }

    private static final Map<String, GeneralSkill> commonSkills = new HashMap<>();
    private static final Map<String, EntityTargetedSkill<?>> entityTargetedSkills = new HashMap<>();

    public static void init() {
        Registrar registrar = new Registrar() {
            @Override
            public void register(GeneralSkill skill) {
                register0(skill);
            }

            @Override
            public void register(EntityTargetedSkill<?> skill) {
                register0(skill);
            }
        };
        registerBuiltIn(registrar);
    }

    private static void register0(GeneralSkill skill) {
        if (commonSkills.putIfAbsent(key(skill), skill) != null) {
            throw new RuntimeException("Duplicate skill registered: " + key(skill));
        }
    }

    private static void register0(EntityTargetedSkill<?> skill) {
        if (entityTargetedSkills.putIfAbsent(key(skill), skill) != null) {
            throw new RuntimeException("Duplicate skill registered: " + key(skill));
        }
    }

    public static GeneralSkill getCommonSkill(String key) {
        GeneralSkill res = commonSkills.get(key);
        if (res == null) {
            throw new RuntimeException("No such key: " + key);
        }
        return res;
    }

    public static EntityTargetedSkill<? extends Entity> getEntityTargetedSkill(String key) {
        EntityTargetedSkill<?> res = entityTargetedSkills.get(key);
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

    /**
     * Use the caller class name as the skill key.
     */
    @Environment(EnvType.CLIENT)
    public static boolean sendCommonSkill(Object... args) {
        try {
            String skillKey = StackWalker.getInstance().walk(stream -> stream.skip(1).findFirst().orElseThrow().getClassName());
            GeneralSkill skill = PlayerSkills.getCommonSkill(skillKey);
            Tag nbtArgs = skill.clientSend(ClientUtils.getClientPlayer(), args);
            ClientNetManager.sendCommonSkill(skillKey, nbtArgs);
            return true;
        } catch (NoPermissionException e) {
            BiologyDictionaryClient.sendCenteredWarning(e.getGameMessage());
        } catch (Exception e) {
            LOGGER.warn(Misc.getStackToString(e));
        }
        return false;
    }

    /**
     * Use the caller class name as the skill key.
     */
    @Environment(EnvType.CLIENT)
    public static boolean sendEntityTargetedSkill(Entity entity, Object... args) {
        try {
            String skillKey = StackWalker.getInstance().walk(stream -> stream.skip(1).findFirst().orElseThrow().getClassName());

            // To avoid delayed refresh of client-side configurations,
            // this validation is not performed on the client side.
            // Permissions.checkSkillNotBanned(skillKey);

            EntityTargetedSkill<?> skill = PlayerSkills.getEntityTargetedSkill(skillKey);
            Tag nbtArgs = skill.clientSend(ClientUtils.getClientPlayer(), Misc.cast(entity), args);
            ClientNetManager.sendEntityTargetedSkill(skillKey, entity, nbtArgs);
            return true;
        } catch (NoPermissionException e) {
            BiologyDictionaryClient.sendCenteredWarning(e.getGameMessage());
        } catch (Exception e) {
            LOGGER.warn(Misc.getStackToString(e));
        }
        return false;
    }

    public interface Registrar {
        void register(GeneralSkill skill);
        void register(EntityTargetedSkill<?> skill);
    }
}
