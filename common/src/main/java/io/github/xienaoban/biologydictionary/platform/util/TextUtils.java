package io.github.xienaoban.biologydictionary.platform.util;

import io.github.xienaoban.biologydictionary.Lang;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public final class TextUtils {
    @Environment(EnvType.CLIENT)
    public static Font getGlobalFont() {
        return Minecraft.getInstance().font;
    }

    public static MutableComponent empty() {
        return Component.empty();
    }

    public static MutableComponent space() {
        return literal(" ");
    }

    public static MutableComponent comma() {
        return translate(Lang.TEXT_COMMA);
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

    public static List<FormattedCharSequence> toLines(FormattedText text, Font font, int maxWidth) {
        return font.split(text, maxWidth);
    }

    public static float getLineHeight(Font font) {
        return getLineHeight(font, 1F);
    }

    public static float getLineHeight(Font font, float scale) {
        return font.lineHeight * scale;
    }

    public static MutableComponent truncateByWidth(Component text, Font font, float maxWidth, float fontSize) {
        String str = text.getString();
        float ellipsisWidth = font.width("...") * fontSize;
        if (ellipsisWidth >= maxWidth) {
            return Component.empty();
        }
        float maxTextWidth = maxWidth - ellipsisWidth;
        float scale = fontSize;
        int end = str.length();
        while (end > 0 && font.width(str.substring(0, end)) * scale > maxTextWidth) {
            end--;
        }
        if (end == str.length()) return text.copy();
        return Component.literal(str.substring(0, end) + "...");
    }
}
