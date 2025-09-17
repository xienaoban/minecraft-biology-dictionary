package io.github.xienaoban.biologydictionary.core.skill;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.core.skill.impl.MobSetNoAiSkill;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public final class Skills {
    private static final Map<ResourceLocation, EntityOrientedSkill> entityOrientedSkills = new HashMap<>();

    public static final ResourceLocation ENTITY_SET_INVULNERABLE = r("ENTITY_SET_INVULNERABLE", new EntityOrientedSkill() {
        @Environment(EnvType.CLIENT) @Override public Tag clientSend(LocalPlayer player, Entity entity, Object... args) {
            boolean inv = (boolean) args[0];
            Permissions.checkPlayerCreative(player);
            return ByteTag.valueOf(inv);
        }
        @Override public void serverReceive(MinecraftServer server, ServerPlayer player, Entity entity, Tag args) {
            boolean inv = args.asBoolean().orElseThrow();
            Permissions.checkPlayerCreative(player);
            EntityUtils.mergeNbt(entity, VanillaEntityProperties.OfEntity.createInvulnerableProperty().toNbtWith(inv));
        }
    });

    public static final ResourceLocation ENTITY_SET_SOUND = r("ENTITY_SET_SOUND", new EntityOrientedSkill() {
        @Environment(EnvType.CLIENT) @Override public Tag clientSend(LocalPlayer player, Entity entity, Object... args) {
            boolean silent = (boolean) args[0];
            return ByteTag.valueOf(silent);
        }
        @Override public void serverReceive(MinecraftServer server, ServerPlayer player, Entity entity, Tag args) {
            boolean silent = args.asBoolean().orElseThrow();
            EntityUtils.mergeNbt(entity, VanillaEntityProperties.OfEntity.createSilentProperty().toNbtWith(silent));
        }
    });

    public static final ResourceLocation ENTITY_SET_PORTAL_COOLDOWN = r("ENTITY_SET_PORTAL_COOLDOWN", new EntityOrientedSkill() {
        @Environment(EnvType.CLIENT) @Override public Tag clientSend(LocalPlayer player, Entity entity, Object... args) {
            int cooldown = (int) args[0];
            return IntTag.valueOf(cooldown);
        }
        @Override public void serverReceive(MinecraftServer server, ServerPlayer player, Entity entity, Tag args) {
            int cooldown = args.asInt().orElseThrow();
            EntityUtils.mergeNbt(entity, VanillaEntityProperties.OfEntity.createPortalCooldownProperty().toNbtWith(cooldown));
        }
    });

    public static final ResourceLocation MOB_SET_NO_AI = r("MOB_SET_NO_AI", new MobSetNoAiSkill());

    public static final int AGEABLE_MOB_SET_FORCED_AGE_EXP = 4;
    public static final ResourceLocation AGEABLE_MOB_SET_FORCED_AGE = r("AGEABLE_MOB_SET_FORCED_AGE", new EntityOrientedSkill() {

        @Environment(EnvType.CLIENT) @Override public Tag clientSend(LocalPlayer player, Entity entity, Object... args) {
            int forcedAge = (int) args[0];
            int age = (int) args[1];
            Permissions.checkPlayerCreativeOrExperiencePoints(player, AGEABLE_MOB_SET_FORCED_AGE_EXP);
            return new IntArrayTag(new int[] { forcedAge, age });
        }
        @Override public void serverReceive(MinecraftServer server, ServerPlayer player, Entity entity, Tag args) {
            int[] t = args.asIntArray().orElseThrow();
            int forcedAge = t[0];
            int age = t[1];
            Permissions.checkPlayerCreativeOrExperiencePoints(player, AGEABLE_MOB_SET_FORCED_AGE_EXP);

            CompoundTag nbt = new CompoundTag();
            IntProperty<AgeableMob> fap = VanillaEntityProperties.OfAgeableMob.createForcedAgeProperty();
            fap.set(forcedAge);
            fap.writeTo(nbt);
            IntProperty<AgeableMob> ap = VanillaEntityProperties.OfAgeableMob.createAgeProperty();
            ap.set(age);
            ap.writeTo(nbt);

            addExperiencePointsIfNotCreative(player, -AGEABLE_MOB_SET_FORCED_AGE_EXP);
            EntityUtils.mergeNbt(entity, nbt);
        }
    });

    public static final int BEE_CLEAR_HIVE_EXP = 1;
    public static final ResourceLocation BEE_CLEAR_HIVE = r("BEE_CLEAR_HIVE", new EntityOrientedSkill() {
        @Environment(EnvType.CLIENT) @Override public Tag clientSend(LocalPlayer player, Entity entity, Object... args) {
            return ByteTag.valueOf(true);
        }
        @Override public void serverReceive(MinecraftServer server, ServerPlayer player, Entity entity, Tag args) {
            Permissions.checkLegalArg(args.asBoolean().orElseThrow(), false);
            EntityUtils.mergeNbt(entity, VanillaEntityProperties.OfBee.createHivePosProperty().toNbtWith(null));
        }
    });

    /// --------------------------------------------------------------------------------------------------------

    public static void register(ResourceLocation key, EntityOrientedSkill skill) {
        entityOrientedSkills.put(key, skill);
    }

    public static EntityOrientedSkill getSkill(ResourceLocation key) {
        EntityOrientedSkill res = entityOrientedSkills.get(key);
        if (res == null) {
            throw new RuntimeException("No such key: " + key);
        }
        return res;
    }

    public static void addExperiencePointsIfNotCreative(ServerPlayer player, int experience) {
        if (PlayerUtils.isCreative(player)) { return; }
        PlayerUtils.addExperiencePoints(player, experience);
        PlayerUtils.playLocalSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 0.01F);
    }

    public static void addExperienceLevelsIfNotCreative(ServerPlayer player, int experience) {
        if (PlayerUtils.isCreative(player)) { return; }
        PlayerUtils.addExperienceLevels(player, experience);
        PlayerUtils.playLocalSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 0.01F);
    }

    /**
     * Register built-in skills.
     */
    private static ResourceLocation r(String path, EntityOrientedSkill skill) {
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath(Lang.BIOLOGY_DICTIONARY, path.toLowerCase());
        register(key, skill);
        return key;
    }
}
