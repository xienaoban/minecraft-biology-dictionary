package io.github.xienaoban.biologydictionary.core.handler;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.common.util.Pair;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.AgeableMob;

import java.util.HashMap;
import java.util.Map;

public final class PropertyUpdaters {
    private static final Map<ResourceLocation, Handler> handlers = new HashMap<>();

    public static final ResourceLocation ENTITY_SET_SOUND = r("ENTITY_SET_SOUND", new Handler() {
        @Environment(EnvType.CLIENT) @Override public Tag clientSend(Object... args) {
            boolean silent = (boolean) args[0];
            Permissions.checkPlayerCreative(McClientUtils.getClientPlayer());
            return ByteTag.valueOf(silent);
        }
        @Override public Pair<CompoundTag, CompoundTag> serverReceive(Tag args, ServerNetApi.Context ctx) {
            boolean silent = args.asBoolean().orElseThrow();
            Permissions.checkPlayerCreative(ctx.player());
            return Pair.ofFirst(VanillaEntityProperties.OfEntity.createSilentProperty().toNbtWith(silent));
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

    /**
     * Register built-in handlers.
     */
    private static ResourceLocation r(String path, Handler handler) {
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath(Lang.BIOLOGY_DICTIONARY, path.toLowerCase());
        register(key, handler);
        return key;
    }


    public static void addExperienceIfNotCreative(ServerPlayer player, int experience) {
        if (PlayerUtils.isCreative(player)) { return; }
        PlayerUtils.addExperience(player, experience);
        PlayerUtils.playLocalSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 0.01F);
    }

    public interface Handler {
        @Environment(EnvType.CLIENT) Tag clientSend(Object... args);
        Pair<CompoundTag, CompoundTag> serverReceive(Tag args, ServerNetApi.Context ctx);
    }
}
