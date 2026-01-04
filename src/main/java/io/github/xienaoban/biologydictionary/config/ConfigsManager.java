package io.github.xienaoban.biologydictionary.config;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigCategory;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigEntry;
import net.fabricmc.loader.api.FabricLoader;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages configuration lifecycle for Biology Dictionary.
 * Handles loading, saving, and remote config synchronization.
 */
public final class ConfigsManager {
    private static final Configs INSTANCE = new Configs();
    private static Configs.ServerConfigs serverConfigs = INSTANCE.getServer();
    private static final Configs.ClientConfigs clientConfigs = INSTANCE.getClient();

    private ConfigsManager() {} // Utility class

    /**
     * Get the configuration instance.
     */
    public static Configs getInstance() {
        return INSTANCE;
    }

    /**
     * Get the active server configuration.
     * Returns remote config if connected to a server, or local config otherwise.
     */
    public static Configs.ServerConfigs getServer() {
        return serverConfigs;
    }

    /**
     * Get the client configuration.
     */
    public static Configs.ClientConfigs getClient() {
        return clientConfigs;
    }

    /**
     * Set remote server configuration from server.
     * Called when receiving config packet from server.
     */
    public static void setRemoteServerConfigs(Configs.ServerConfigs remoteConfigs) {
        serverConfigs = remoteConfigs;
        LOGGER.info("Using remote server configs");
    }

    /**
     * Reset to local server configuration.
     * Called when disconnecting from a server or in singleplayer.
     */
    public static void setLocalServerConfigs() {
        serverConfigs = INSTANCE.getServer();
        LOGGER.info("Using local server configs");
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(Lang.CONFIG_FILE);
    }

    /**
     * Save configuration to YAML file.
     */
    public static void save() {
        Path configPath = getConfigPath();
        try {
            Files.createDirectories(configPath.getParent());

            Map<String, Object> data = new HashMap<>();

            // Iterate through fields annotated with @ConfigCategory
            for (Field categoryField : INSTANCE.getClass().getDeclaredFields()) {
                if (categoryField.isAnnotationPresent(ConfigCategory.class)) {
                    categoryField.setAccessible(true);
                    Object categoryObject = categoryField.get(INSTANCE);
                    String fieldName = categoryField.getName();
                    data.put(fieldName, saveConfigCategoryToMap(categoryObject));
                }
            }

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);

            Yaml yaml = new Yaml(options);
            try (FileWriter writer = new FileWriter(configPath.toFile())) {
                yaml.dump(data, writer);
            }

            LOGGER.info("Configuration saved to {}", configPath.toAbsolutePath());
        } catch (IOException | IllegalAccessException e) {
            LOGGER.error("Failed to save configuration", e);
        }
    }

    /**
     * Load configuration from YAML file.
     */
    public static void load() {
        Path configPath = getConfigPath();
        if (!Files.exists(configPath)) {
            save(); // Create default config
            return;
        }

        try (FileInputStream input = new FileInputStream(configPath.toFile())) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(input);

            if (data != null) {
                // Iterate through fields annotated with @ConfigCategory
                for (Field categoryField : INSTANCE.getClass().getDeclaredFields()) {
                    if (categoryField.isAnnotationPresent(ConfigCategory.class)) {
                        String fieldName = categoryField.getName();
                        Map<?, ?> categoryData = (Map<?, ?>) data.get(fieldName);
                        if (categoryData != null) {
                            categoryField.setAccessible(true);
                            Object categoryObject = categoryField.get(INSTANCE);
                            loadConfigCategoryFromMap(categoryData, categoryObject);
                        }
                    }
                }
            }

            LOGGER.info("Configuration loaded from {}", configPath);
        } catch (IOException | IllegalAccessException e) {
            LOGGER.error("Failed to load configuration", e);
            save(); // Create default config on error
        }
    }

    /**
     * Convert config object to Map using reflection to avoid type tags in YAML.
     */
    private static Map<String, Object> saveConfigCategoryToMap(Object configObject) {
        Map<String, Object> map = new HashMap<>();
        for (Field field : configObject.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigEntry.class)) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(configObject);
                    if (value instanceof Enum<?> e) {
                        value = e.name();
                    }
                    map.put(field.getName(), value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return map;
    }

    private static void loadConfigCategoryFromMap(Map<?, ?> dataMap, Object configObject) {
        for (Map.Entry<?, ?> entry : dataMap.entrySet()) {
            try {
                Field field = configObject.getClass().getDeclaredField((String) entry.getKey());
                field.setAccessible(true);
                Object convertedValue = Misc.convertValue(entry.getValue(), field.getType());
                field.set(configObject, convertedValue);
            } catch (NoSuchFieldException e) {
                LOGGER.warn("Unknown or deprecated entry: {}", entry.getKey());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
