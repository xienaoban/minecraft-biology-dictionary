package io.github.xienaoban.biologydictionary.core.skill;

import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.skill.general.*;
import io.github.xienaoban.biologydictionary.core.skill.entity.*;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public final class Skills {
    private static final Map<String, GeneralSkill> commonSkills = new HashMap<>();
    private static final Map<String, EntityTargetedSkill<?>> entityOrientedSkills = new HashMap<>();

    public static void init() {
        register(new HighlightEntitiesSkill());
        register(new GetSpawnEggSkill());

        register(new EntitySetVariantSkill());
        register(new EntitySetInvulnerableSkill());
        register(new EntitySetSoundSkill());
        register(new EntitySetPortalCooldownSkill());
        register(new MobSetNoAiSkill());
        register(new MobForcePersistentSkill());
        register(new LivingEntityStealInventorySkill());
        register(new SheepForceEatGrassSkill());
        register(new AgeableMobSetForcedAgeSkill());
        register(new BeeClearHiveSkill());
        register(new EntityGiftPetSkill());
        register(new VillagerForceRestockSkill());
        register(new WanderingTraderRetainSkill());
    }

    public static void register(GeneralSkill skill) {
        if (commonSkills.putIfAbsent(key(skill), skill) != null) {
            throw new RuntimeException("Duplicate skill registered: " + key(skill));
        }
    }

    public static void register(EntityTargetedSkill<?> skill) {
        if (entityOrientedSkills.putIfAbsent(key(skill), skill) != null) {
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

    public static EntityTargetedSkill<? extends Entity> getEntityOrientedSkill(String key) {
        EntityTargetedSkill<?> res = entityOrientedSkills.get(key);
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
        String skillKey = StackWalker.getInstance().walk(stream -> stream.skip(1).findFirst().orElseThrow().getClassName());
        return ClientNetManager.sendCommonSkill(skillKey, args);
    }

    /**
     * Use the caller class name as the skill key.
     */
    @Environment(EnvType.CLIENT)
    public static boolean sendEntityOrientedSkill(Entity entity, Object... args) {
        String skillKey = StackWalker.getInstance().walk(stream -> stream.skip(1).findFirst().orElseThrow().getClassName());
        return ClientNetManager.sendEntityOrientedSkill(skillKey, entity, args);
    }
}
