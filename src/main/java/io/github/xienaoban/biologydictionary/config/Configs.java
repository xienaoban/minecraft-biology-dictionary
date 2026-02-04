package io.github.xienaoban.biologydictionary.config;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.annotation.Config;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigCategory;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigEntry;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Main configuration data class for Biology Dictionary.
 * Uses annotations for automatic YAML serialization and Cloth Config generation.
 * <p>
 * I'm not going to use @Environment(EnvType.CLIENT) here, or things will get too complicated.
 */
@Config(Lang.CONFIG_TITLE)
public final class Configs {

    /**
     * Client-side configuration options.
     * These settings affect local rendering and behavior.
     */
    public static class ClientConfigs {
        @ConfigEntry
        float screenScale = 1F;

        @ConfigEntry
        FirstPersonShoulderEntityPosition firstPersonShoulderEntityPosition = FirstPersonShoulderEntityPosition.BOTTOM;

        public float getScreenScale() {
            return screenScale;
        }

        public FirstPersonShoulderEntityPosition getFirstPersonShoulderEntityPosition() {
            return firstPersonShoulderEntityPosition;
        }

        public enum FirstPersonShoulderEntityPosition {
            NONE, BOTTOM, SIDES, TOP
        }
    }

    /**
     * Server-side configuration options.
     * These settings are used when running a server or singleplayer.
     */
    public static class ServerConfigs {
        @ConfigEntry
        boolean bookItemRequired = true;

        @ConfigEntry
        boolean bookItemObtainableFromWanderingTrader = true;

        @ConfigEntry
        Set<String> bannedPlayerSkills = Set.of();

        public boolean isBookItemRequired() {
            return bookItemRequired;
        }

        public boolean isBookItemObtainableFromWanderingTrader() {
            return bookItemObtainableFromWanderingTrader;
        }

        public Set<String> getBannedPlayerSkills() {
            return bannedPlayerSkills;
        }
    }

    @ConfigCategory(Lang.CONFIG_CATEGORY_CLIENT)
    private final ClientConfigs client = new ClientConfigs();

    @ConfigCategory(Lang.CONFIG_CATEGORY_SERVER)
    private final ServerConfigs server = new ServerConfigs();

    public ClientConfigs getClient() {
        return client;
    }

    public ServerConfigs getServer() {
        return server;
    }

    // ==================== Translation Key Utilities ====================

    public static String getConfigNameTranslationKey(String fieldName) {
        return Lang.CONFIG_ENTRY_PREFIX + fieldName;
    }

    public static String getEnumValueTranslationKey(Enum<?> enumValue) {
        return "enum." + enumValue.getClass().getName().replace('$', '.') + "." + enumValue.name();
    }

    // ==================== Config Entry Traversal ====================

    /**
     * Iterate through all config entries in this configs instance.
     * This provides a unified way to access all configuration entries.
     *
     * @param categoryConsumer Consumer that receives category name and entry info
     */
    public void forEachConfigEntry(BiConsumer<String, ConfigEntryInfo> categoryConsumer) {
        for (Field categoryField : getClass().getDeclaredFields()) {
            if (categoryField.isAnnotationPresent(ConfigCategory.class)) {
                try {
                    categoryField.setAccessible(true);
                    Object categoryObject = categoryField.get(this);

                    for (Field entryField : categoryObject.getClass().getDeclaredFields()) {
                        if (entryField.isAnnotationPresent(ConfigEntry.class)) {
                            ConfigEntry annotation = entryField.getAnnotation(ConfigEntry.class);
                            ConfigEntryInfo info = new ConfigEntryInfo(entryField, annotation, categoryObject);
                            categoryConsumer.accept(categoryField.getName(), info);
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access config field", e);
                }
            }
        }
    }

    /**
     * Iterate through all config entries in a specific category object.
     * This is useful for iterating through config objects that might be remote copies.
     *
     * @param categoryObject The config category object (e.g., ServerConfigs)
     * @param consumer Consumer that receives entry info for each config entry
     */
    public static void forEachConfigEntryInCategory(Object categoryObject, Consumer<ConfigEntryInfo> consumer) {
        for (Field entryField : categoryObject.getClass().getDeclaredFields()) {
            if (entryField.isAnnotationPresent(ConfigEntry.class)) {
                ConfigEntry annotation = entryField.getAnnotation(ConfigEntry.class);
                ConfigEntryInfo info = new ConfigEntryInfo(entryField, annotation, categoryObject);
                consumer.accept(info);
            }
        }
    }

    /**
     * Represents a single configuration entry with its metadata.
     */
    public record ConfigEntryInfo(Field field, ConfigEntry annotation, Object categoryObject) {
        public String getName() {
            return field.getName();
        }

        public Class<?> getType() {
            return field.getType();
        }

        public Object getValue() {
            return getValue(categoryObject);
        }

        public Object getValue(Object anotherCategoryObject) {
            try {
                field.setAccessible(true);
                return field.get(anotherCategoryObject);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to read config value", e);
            }
        }
    }
}
