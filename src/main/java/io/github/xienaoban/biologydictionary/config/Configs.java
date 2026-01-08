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
     * Server-side configuration options.
     * These settings are used when running a server or singleplayer.
     */
    public static class ServerConfigs {
        @ConfigEntry
        Set<String> bannedPlayerSkills = Set.of();

        public Set<String> getBannedPlayerSkills() {
            return bannedPlayerSkills;
        }
    }

    /**
     * Client-side configuration options.
     * These settings affect local rendering and behavior.
     */
    public static class ClientConfigs {
        @ConfigEntry
        FirstPersonShoulderEntityPosition firstPersonShoulderEntityPosition = FirstPersonShoulderEntityPosition.BOTTOM;

        public FirstPersonShoulderEntityPosition getFirstPersonShoulderEntityPosition() {
            return firstPersonShoulderEntityPosition;
        }
    }

    @ConfigCategory(Lang.CONFIG_CATEGORY_SERVER)
    private final ServerConfigs server = new ServerConfigs();

    @ConfigCategory(Lang.CONFIG_CATEGORY_CLIENT)
    private final ClientConfigs client = new ClientConfigs();

    public ServerConfigs getServer() {
        return server;
    }

    public ClientConfigs getClient() {
        return client;
    }

    public enum FirstPersonShoulderEntityPosition {
        NONE, BOTTOM, SIDES, TOP
    }

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
            try {
                field.setAccessible(true);
                return field.get(categoryObject);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to read config value", e);
            }
        }
    }
}
