package io.github.xienaoban.biologydictionary.core.handler;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.common.util.Pair;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.CodecProperty;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public final class PropertyUpdaters {
    private static final Map<ResourceLocation, Handler> handlers = new HashMap<>();

    public static final ResourceLocation ENTITY_SET_INVULNERABLE = r("ENTITY_SET_INVULNERABLE", new Handler() {
        @Environment(EnvType.CLIENT) @Override public Tag clientSend(Object... args) {
            boolean inv = (boolean) args[0];
            Permissions.checkPlayerCreative(McClientUtils.getClientPlayer());
            return ByteTag.valueOf(inv);
        }
        @Override public Pair<CompoundTag, CompoundTag> serverReceive(Tag args, ServerNetApi.Context ctx) {
            boolean inv = args.asBoolean().orElseThrow();
            Permissions.checkPlayerCreative(ctx.player());
            return Pair.ofFirst(VanillaEntityProperties.OfEntity.createInvulnerableProperty().toNbtWith(inv));
        }
    });

    public static final ResourceLocation ENTITY_SET_SOUND = r("ENTITY_SET_SOUND", new Handler() {
        @Environment(EnvType.CLIENT) @Override public Tag clientSend(Object... args) {
            boolean silent = (boolean) args[0];
            return ByteTag.valueOf(silent);
        }
        @Override public Pair<CompoundTag, CompoundTag> serverReceive(Tag args, ServerNetApi.Context ctx) {
            boolean silent = args.asBoolean().orElseThrow();
            return Pair.ofFirst(VanillaEntityProperties.OfEntity.createSilentProperty().toNbtWith(silent));
        }
    });

    public static final ResourceLocation ENTITY_SET_PORTAL_COOLDOWN = r("ENTITY_SET_PORTAL_COOLDOWN", new Handler() {
        @Environment(EnvType.CLIENT) @Override public Tag clientSend(Object... args) {
            int cooldown = (int) args[0];
            return IntTag.valueOf(cooldown);
        }
        @Override public Pair<CompoundTag, CompoundTag> serverReceive(Tag args, ServerNetApi.Context ctx) {
            int cooldown = args.asInt().orElseThrow();
            return Pair.ofFirst(VanillaEntityProperties.OfEntity.createPortalCooldownProperty().toNbtWith(cooldown));
        }
    });

    public static final ResourceLocation MOB_SET_NO_AI = r("MOB_SET_NO_AI", new Handler() {
        @Environment(EnvType.CLIENT) @Override public Tag clientSend(Object... args) {
            boolean noAi = (boolean) args[0];
            Permissions.checkPlayerCreative(McClientUtils.getClientPlayer());
            return ByteTag.valueOf(noAi);
        }
        @Override public Pair<CompoundTag, CompoundTag> serverReceive(Tag args, ServerNetApi.Context ctx) {
            boolean noAi = args.asBoolean().orElseThrow();
            Permissions.checkPlayerCreative(ctx.player());
            CompoundTag nbt = VanillaEntityProperties.OfMob.createNoAiProperty().toNbtWith(noAi);

            // Clear the motion caused by collisions accumulated during the AI-disabled period
            // to prevent the entity from flying around randomly.
            CodecProperty<Entity, Vec3> motionProperty = VanillaEntityProperties.OfEntity.createMotionProperty();
            motionProperty.set(Vec3.ZERO);
            motionProperty.writeTo(nbt);

            return Pair.ofFirst(nbt);
        }
    });

    public static final ResourceLocation AGEABLE_MOB_SET_FORCED_AGE = r("AGEABLE_MOB_SET_FORCED_AGE", new Handler() {
        private static final int EXP = 4;

        @Environment(EnvType.CLIENT) @Override public Tag clientSend(Object... args) {
            int forcedAge = (int) args[0];
            int age = (int) args[1];
            Permissions.checkPlayerCreativeOrExperience(McClientUtils.getClientPlayer(), EXP);
            return new IntArrayTag(new int[] { forcedAge, age });
        }
        @Override public Pair<CompoundTag, CompoundTag> serverReceive(Tag args, ServerNetApi.Context ctx) {
            int[] t = args.asIntArray().orElseThrow();
            int forcedAge = t[0];
            int age = t[1];
            Permissions.checkPlayerCreativeOrExperience(ctx.player(), EXP);

            CompoundTag nbt = new CompoundTag();
            IntProperty<AgeableMob> fap = VanillaEntityProperties.OfAgeableMob.createForcedAgeProperty();
            fap.set(forcedAge);
            fap.writeTo(nbt);
            IntProperty<AgeableMob> ap = VanillaEntityProperties.OfAgeableMob.createAgeProperty();
            ap.set(age);
            ap.writeTo(nbt);

            addExperienceIfNotCreative(ctx.player(), -EXP);
            return Pair.ofFirst(nbt);
        }
    });

    public static final ResourceLocation BEE_CLEAR_HIVE = r("BEE_CLEAR_HIVE", new Handler() {
        @Environment(EnvType.CLIENT) @Override public Tag clientSend(Object... args) {
            return ByteTag.valueOf(true);
        }
        @Override public Pair<CompoundTag, CompoundTag> serverReceive(Tag args, ServerNetApi.Context ctx) {
            Permissions.checkLegalArg(args.asBoolean().orElseThrow(), false);
            return Pair.ofFirst(VanillaEntityProperties.OfBee.createHivePosProperty().toNbtWith(null));
        }
    });

    public static void register(ResourceLocation key, Handler handler) {
        handlers.put(key, handler);
    }

    public static Handler getHandler(ResourceLocation key) {
        Handler res = handlers.get(key);
        if (res == null) {
            throw new RuntimeException("No such key: " + key);
        }
        return res;
    }

    public static void addExperienceIfNotCreative(ServerPlayer player, int experience) {
        if (PlayerUtils.isCreative(player)) { return; }
        PlayerUtils.addExperience(player, experience);
        PlayerUtils.playLocalSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 0.01F);
    }

    /**
     * Register built-in handlers.
     */
    private static ResourceLocation r(String path, Handler handler) {
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath(Lang.BIOLOGY_DICTIONARY, path.toLowerCase());
        register(key, handler);
        return key;
    }

    public interface Handler {
        @Environment(EnvType.CLIENT) Tag clientSend(Object... args);
        Pair<CompoundTag, CompoundTag> serverReceive(Tag args, ServerNetApi.Context ctx);
    }
}
