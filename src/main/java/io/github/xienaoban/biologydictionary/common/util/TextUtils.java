package io.github.xienaoban.biologydictionary.common.util;

import io.github.xienaoban.biologydictionary.Lang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

public final class TextUtils {
    public static MutableComponent empty() {
        return Component.empty();
    }

    public static MutableComponent comma() {
        return translate(Lang.TEXT_COMMA);
    }

    public static MutableComponent literal(String text) {
        return Component.literal(text);
    }

    public static MutableComponent translate(String key) {
        return Component.translatable(key);
    }

    public static MutableComponent translate(String key, Object... args) {
        return Component.translatable(key, args);
    }

    @SafeVarargs
    public static <T extends Component> MutableComponent concat(T... texts) {
        return concat(Arrays.asList(texts), empty());
    }

    public static <T extends Component> MutableComponent concat(Collection<? extends T> collection) {
        return concat(collection, empty());
    }

    public static <T extends Component> MutableComponent concat(Collection<? extends T> collection, Component separator) {
        return concat(collection, separator, Function.identity());
    }

    public static <T extends Component> MutableComponent concat(Collection<? extends T> collection, Component separator, Function<T, Component> function) {
        return ComponentUtils.formatList(collection, separator, function);
    }
}
