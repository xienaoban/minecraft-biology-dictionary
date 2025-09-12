package io.github.xienaoban.biologydictionary.core.handler;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.common.util.Pair;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class PropertyUpdaters {
    private static final Map<ResourceLocation, Handler> handlers = new HashMap<>();

    public static final ResourceLocation ENTITY_SET_SOUND = r("entity_set_sound", new Handler() {
        @Environment(EnvType.CLIENT) @Override public Tag clientSend(Object... args) {
            boolean silent = (boolean) args[0];
            Permissions.checkPlayerCreativeMode(McClientUtils.getClientPlayer());
            return ByteTag.valueOf(silent);
        }
        @Override public Pair<CompoundTag, CompoundTag> serverReceive(Tag args, ServerNetApi.Context ctx) {
            boolean silent = args.asBoolean().orElseThrow();
            Permissions.checkPlayerCreativeMode(ctx.player());
            return Pair.ofFirst(VanillaEntityProperties.OfEntity.createSilentProperty().toNbtWith(silent));
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
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath(Lang.BIOLOGY_DICTIONARY, path);
        register(key, handler);
        return key;
    }

    public interface Handler {
        @Environment(EnvType.CLIENT) Tag clientSend(Object... args);
        Pair<CompoundTag, CompoundTag> serverReceive(Tag args, ServerNetApi.Context ctx);
    }
}
