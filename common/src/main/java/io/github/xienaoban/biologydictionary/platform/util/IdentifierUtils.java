package io.github.xienaoban.biologydictionary.platform.util;

import com.mojang.serialization.Codec;
import io.github.xienaoban.biologydictionary.BiologyDictionary;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class IdentifierUtils {
    private IdentifierUtils() {}

    public static ResourceLocation mc(String path) {
        return new ResourceLocation("minecraft", path);
    }

    public static ResourceLocation bd(String path) {
        return create(BiologyDictionary.MOD_ID, path);
    }

    public static ResourceLocation create(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    public static ResourceLocation fromStringOrNull(String toDeserialize) {
        return ResourceLocation.tryParse(toDeserialize);
    }

    public static ResourceLocation fromString(String toDeserialize) {
        return new ResourceLocation(toDeserialize);
    }

    public static String toString(ResourceLocation toSerialize) {
        return toSerialize.toString();
    }

    public static ResourceLocation fromBuf(FriendlyByteBuf buf) {
        return fromString(buf.readUtf());
    }

    public static void toBuf(FriendlyByteBuf buf, ResourceLocation toSerialize) {
        buf.writeUtf(toString(toSerialize));
    }

    public static ResourceLocation fromNbt(StringTag tag) {
        return fromString(tag.getAsString());
    }

    public static StringTag toNbt(ResourceLocation toSerialize) {
        return StringTag.valueOf(toString(toSerialize));
    }

    public static boolean isMc(ResourceLocation id) {
        return ResourceLocation.DEFAULT_NAMESPACE.equals(id.getNamespace());
    }

    public static boolean isBd(ResourceLocation id) {
        return BiologyDictionary.MOD_ID.equals(id.getNamespace());
    }

    public static Codec<ResourceLocation> codec() {
        return ResourceLocation.CODEC;
    }
}
