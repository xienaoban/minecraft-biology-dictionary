package io.github.xienaoban.biologydictionary.platform.util;

import com.mojang.serialization.Codec;
import io.github.xienaoban.biologydictionary.BiologyDictionary;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public final class IdentifierUtils {
    private IdentifierUtils() {}

    public static Identifier mc(String path) {
        return Identifier.withDefaultNamespace(path);
    }

    public static Identifier bd(String path) {
        return create(BiologyDictionary.MOD_ID, path);
    }

    public static Identifier create(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    public static Identifier fromStringOrNull(String toDeserialize) {
        return Identifier.tryParse(toDeserialize);
    }

    public static Identifier fromString(String toDeserialize) {
        return Identifier.parse(toDeserialize);
    }

    public static String toString(Identifier toSerialize) {
        return toSerialize.toString();
    }

    public static Identifier fromBuf(FriendlyByteBuf buf) {
        return fromString(buf.readUtf());
    }

    public static void toBuf(FriendlyByteBuf buf, Identifier toSerialize) {
        buf.writeUtf(toString(toSerialize));
    }

    public static Identifier fromNbt(StringTag tag) {
        return fromString(tag.value());
    }

    public static StringTag toNbt(Identifier toSerialize) {
        return StringTag.valueOf(toString(toSerialize));
    }

    public static boolean isMc(Identifier id) {
        return Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace());
    }

    public static boolean isBd(Identifier id) {
        return BiologyDictionary.MOD_ID.equals(id.getNamespace());
    }

    public static Codec<Identifier> codec() {
        return Identifier.CODEC;
    }
}
