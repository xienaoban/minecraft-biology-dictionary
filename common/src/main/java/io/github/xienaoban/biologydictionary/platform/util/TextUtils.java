package io.github.xienaoban.biologydictionary.platform.util;

import io.github.xienaoban.biologydictionary.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public final class TextUtils {
    private static final String DEFAULT_FALLBACK_LANGUAGE = "en_us";
    private static final Map<String, Map<String, String>> LANGUAGE_FALLBACKS = new ConcurrentHashMap<>();

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

    public static MutableComponent withFallbacks(Component input) {
        return withFallbacks(input, DEFAULT_FALLBACK_LANGUAGE);
    }

    public static MutableComponent withFallbacks(Component input, String language) {
        return withFallbacks(input, getLanguageFallbackMap(
                language == null ? DEFAULT_FALLBACK_LANGUAGE : language));
    }

    public static MutableComponent withFallbacks(Component input, ServerPlayer player) {
        return withFallbacks(input, player == null ? DEFAULT_FALLBACK_LANGUAGE : player.clientInformation().language());
    }

    private static MutableComponent withFallbacks(Component input, Map<String, String> fallbacks) {
        ComponentContents contents = input.getContents();
        if (contents instanceof TranslatableContents translatable) {
            String fallback = translatable.getFallback();
            if (fallback == null) {
                fallback = fallbacks.getOrDefault(translatable.getKey(), translatable.getKey());
            }
            Object[] args = translatable.getArgs();
            Object[] fallbackArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                fallbackArgs[i] = arg instanceof Component component
                        ? withFallbacks(component, fallbacks)
                        : arg;
            }
            contents = new TranslatableContents(translatable.getKey(), fallback, fallbackArgs);
        }

        Style style = input.getStyle();
        HoverEvent hoverEvent = style.getHoverEvent();
        if (hoverEvent != null) {
            Component showText = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
            if (showText != null) {
                style = style.withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT, withFallbacks(showText, fallbacks)));
            }
        }

        MutableComponent result = MutableComponent.create(contents).setStyle(style);
        for (Component sibling : input.getSiblings()) {
            result.append(withFallbacks(sibling, fallbacks));
        }
        return result;
    }

    public static MutableComponent modLog(Component message) {
        return concat(translate(Lang.TEXT_INFO_FROM_THIS_MOD).withStyle(ChatFormatting.DARK_GREEN), message);
    }

    private static Map<String, String> getLanguageFallbackMap(String language) {
        Map<String, String> fallbacks = LANGUAGE_FALLBACKS.get(language);
        if (fallbacks != null) {
            return fallbacks;
        }

        Map<String, String> loaded = loadLanguageFallbackFile(language);
        if (loaded == null && !DEFAULT_FALLBACK_LANGUAGE.equals(language)) {
            loaded = getLanguageFallbackMap(DEFAULT_FALLBACK_LANGUAGE);
        }
        if (loaded == null) {
            loaded = Map.of();
        }

        Map<String, String> existing = LANGUAGE_FALLBACKS.putIfAbsent(language, loaded);
        return existing != null ? existing : loaded;
    }

    private static Map<String, String> loadLanguageFallbackFile(String language) {
        Map<String, String> translations = new HashMap<>();
        String path = "/assets/biologydictionary/lang/" + language + ".json";
        try (InputStream stream = TextUtils.class.getResourceAsStream(path)) {
            if (stream == null) {
                LOGGER.warn("Language fallback resource is missing: {}", path);
                return null;
            }
            Language.loadFromJson(stream, translations::put);
        } catch (Exception e) {
            LOGGER.warn("Failed to load language fallback: {}", path, e);
            return null;
        }
        return Map.copyOf(translations);
    }

    public static final class FallbackCache {
        private final Component source;
        private final Map<String, Component> cache = new HashMap<>();

        public FallbackCache(Component source) {
            this.source = source;
        }

        public Component get(String language) {
            String key = language == null ? DEFAULT_FALLBACK_LANGUAGE : language;
            return cache.computeIfAbsent(key, value -> withFallbacks(source, value));
        }

        public Component get(ServerPlayer player) {
            return get(player == null ? null : player.clientInformation().language());
        }
    }
}
