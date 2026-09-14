package io.github.xienaoban.biologydictionary.platform.util;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

@ClientOnly
public final class FontUtils {
    public static Font getGlobalFont() {
        return Minecraft.getInstance().font;
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

    public static MutableComponent truncateByWidth(Component text, float maxWidth, float fontSize) {
        return truncateByWidth(text, getGlobalFont(), maxWidth, fontSize);
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
        if (end == str.length()) { return text.copy(); }
        return Component.literal(str.substring(0, end) + "...");
    }
}
