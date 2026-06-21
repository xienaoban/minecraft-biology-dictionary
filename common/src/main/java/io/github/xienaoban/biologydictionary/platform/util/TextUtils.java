package io.github.xienaoban.biologydictionary.platform.util;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

public final class TextUtils {
    private static final String TEXT_COMMA = "text.biologydictionary.comma";

    public static MutableComponent empty() {
        return Component.empty();
    }

    public static MutableComponent space() {
        return literal(" ");
    }

    public static MutableComponent comma() {
        return translate(TEXT_COMMA);
    }

    public static MutableComponent newline() {
        return literal("\n");
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

    public static boolean hasTranslation(String key) {
        return Language.getInstance().has(key);
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
