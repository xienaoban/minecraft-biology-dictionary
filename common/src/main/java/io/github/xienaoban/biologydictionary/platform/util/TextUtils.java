package io.github.xienaoban.biologydictionary.platform.util;

import io.github.xienaoban.biologydictionary.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.io.InputStream;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public final class TextUtils {
    private static Map<String, String> serverLanguageFallback;

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

    public static <T extends Component> MutableComponent concat(
            Collection<? extends T> collection, Component separator) {
        return concat(collection, separator, Function.identity());
    }

    public static <T extends Component> MutableComponent concat(
            Collection<? extends T> collection, Component separator, Function<T, Component> function) {
        return ComponentUtils.formatList(collection, separator, function);
    }

    public static MutableComponent withFallbacks(Component input) {
        ComponentContents contents = input.getContents();
        if (contents instanceof TranslatableContents translatable) {
            String fallback = translatable.getFallback();
            if (fallback == null) {
                fallback = getServerLanguageText(translatable.getKey());
            }
            Object[] args = translatable.getArgs();
            Object[] fallbackArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                fallbackArgs[i] = arg instanceof Component component ? withFallbacks(component) : arg;
            }
            contents = new TranslatableContents(translatable.getKey(), fallback, fallbackArgs);
        }

        MutableComponent result = MutableComponent.create(contents).setStyle(input.getStyle());
        for (Component sibling : input.getSiblings()) {
            result.append(withFallbacks(sibling));
        }
        return result;
    }

    public static MutableComponent modLog(Component message) {
        return concat(translate(Lang.TEXT_INFO_FROM_THIS_MOD).withStyle(ChatFormatting.DARK_GREEN), message);
    }

    /**
     * Resolve a translation in the server's current language. Dedicated servers only have
     * vanilla en_us loaded, so fall back to the mod's en_us entries when the key is absent.
     */
    private static String getServerLanguageText(String key) {
        Language language = Language.getInstance();
        if (language.has(key)) {
            return language.getOrDefault(key);
        }
        return getServerLanguageFallback().getOrDefault(key, key);
    }

    private static Map<String, String> getServerLanguageFallback() {
        Map<String, String> fallback = serverLanguageFallback;
        if (fallback != null) {
            return fallback;
        }
        Map<String, String> loaded = loadServerLanguageFallback();
        VarHandle.storeStoreFence();
        serverLanguageFallback = loaded;
        return loaded;
    }

    private static Map<String, String> loadServerLanguageFallback() {
        Map<String, String> translations = new HashMap<>();
        try (InputStream stream = TextUtils.class.getResourceAsStream("/assets/biologydictionary/lang/en_us.json")) {
            if (stream == null) {
                LOGGER.warn("Server language fallback resource is missing.");
                return Map.of();
            }
            Language.loadFromJson(stream, translations::put);
        } catch (Exception e) {
            LOGGER.warn("Failed to load server language fallback.", e);
            return Map.of();
        }
        return Map.copyOf(translations);
    }
}
