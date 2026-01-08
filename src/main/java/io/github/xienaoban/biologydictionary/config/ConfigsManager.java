package io.github.xienaoban.biologydictionary.config;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.util.DevUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigCategory;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
     * Get the local configuration instance.
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
     * Get the active client configuration.
     * Not like the server configs, it never changes.
     */
    @Environment(EnvType.CLIENT)
    public static Configs.ClientConfigs getClient() {
        return clientConfigs;
    }

    /**
     * Set remote server configuration from server.
     * Called when receiving config packet from server.
     */
    @Environment(EnvType.CLIENT)
    public static void setRemoteServerConfigs(Configs.ServerConfigs remoteConfigs) {
        serverConfigs = remoteConfigs;
        LOGGER.info("Using remote server configs");
    }

    /**
     * Reset to local server configuration.
     * Called when disconnecting from a server or in singleplayer.
     */
    @Environment(EnvType.CLIENT)
    public static void setLocalServerConfigs() {
        serverConfigs = INSTANCE.getServer();
        LOGGER.info("Using local server configs");
    }

    private static Path getConfigPath() {
        return DevUtils.getConfigDir().resolve(Lang.CONFIG_FILE);
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

            boolean allGood = true;
            if (data != null) {
                // Iterate through fields annotated with @ConfigCategory
                for (Field categoryField : INSTANCE.getClass().getDeclaredFields()) {
                    if (categoryField.isAnnotationPresent(ConfigCategory.class)) {
                        String fieldName = categoryField.getName();
                        Map<?, ?> categoryData = (Map<?, ?>) data.get(fieldName);
                        if (categoryData != null) {
                            categoryField.setAccessible(true);
                            Object categoryObject = categoryField.get(INSTANCE);
                            allGood = loadConfigCategoryFromMap(categoryData, categoryObject);
                        }
                    }
                }
            }
            LOGGER.info("Configuration loaded from {}", configPath);

            if (!allGood) {
                LOGGER.warn("Not all the configuration entries are legal. Refresh the configuration file.");
                save();
            }
        } catch (IOException | IllegalAccessException e) {
            LOGGER.error("Failed to load configuration", e);
            save(); // Create default config on error
        }
    }

    /**
     * Convert config object to Map using reflection to avoid type tags in YAML.
     */
    private static Map<String, Object> saveConfigCategoryToMap(Object configObject) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Field field : configObject.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigEntry.class)) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(configObject);

                    // special cases
                    if (value instanceof Enum<?> e) {
                        value = e.name();
                    } else if (value instanceof Set<?> s) {
                        value = s.stream().sorted().toList();
                    }

                    map.put(field.getName(), value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return map;
    }

    private static boolean loadConfigCategoryFromMap(Map<?, ?> dataMap, Object configObject) {
        boolean allGood = true;
        for (Map.Entry<?, ?> entry : dataMap.entrySet()) {
            try {
                Field field = configObject.getClass().getDeclaredField((String) entry.getKey());
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object convertedValue = Misc.convertNumber(entry.getValue(), fieldType);

                // special cases
                if (Enum.class.isAssignableFrom(fieldType)) {
                    convertedValue = Enum.valueOf(fieldType.asSubclass(Enum.class), (String) convertedValue);
                } else if (Set.class.isAssignableFrom(fieldType)) {
                    convertedValue = Set.copyOf((Collection<?>) convertedValue);
                }

                field.set(configObject, convertedValue);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException | RuntimeException e) {
                allGood = false;
                LOGGER.warn("Bad configuration entry {}: {}", entry.getKey(), entry.getValue());
                LOGGER.warn(Misc.getStackToString(e));
            }
        }
        return allGood;
    }
}
