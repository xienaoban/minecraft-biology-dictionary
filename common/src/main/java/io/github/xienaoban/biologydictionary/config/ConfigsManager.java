package io.github.xienaoban.biologydictionary.config;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigCategory;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigEntry;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.biologydictionary.platform.ClientAndServer;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.DevUtils;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import io.github.xienaoban.biologydictionary.platform.util.StringUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Manages configuration lifecycle for Biology Dictionary.
 * Handles loading, saving, and remote config synchronization.
 */
public final class ConfigsManager {
    private static final int MAX_YAML_SIZE = 64 * 1024; // 64KB

    private static final Configs INSTANCE = new Configs();
    private static final Configs.ClientConfigs clientConfigs = INSTANCE.getClient();
    private static volatile Configs.ServerConfigs serverConfigs = INSTANCE.getServer();

    private ConfigsManager() {} // Utility class

    /**
     * Get the local configuration instance.
     */
    public static Configs getInstance() {
        return INSTANCE;
    }

    /**
     * Get the active client configuration.
     * Not like the server configs, it never changes.
     */
    public static Configs.ClientConfigs getClient() {
        return clientConfigs;
    }

    /**
     * Get the active server configuration.
     * Returns remote config if connected to a server, or local config otherwise.
     */
    public static Configs.ServerConfigs getServer() {
        return serverConfigs;
    }

    /**
     * Reset to local server configuration.
     * Called when disconnecting from a server or in singleplayer.
     */
    public static void setLocalServerConfigs() {
        serverConfigs = INSTANCE.getServer();
        LOGGER.info("Using local server configs.");
    }

    /**
     * Set remote server configuration from server.
     * Called when receiving config packet from server.
     */
    public static void setRemoteServerConfigs(Configs.ServerConfigs remoteConfigs) {
        Objects.requireNonNull(remoteConfigs);
        if (WorldSession.get() == null) {
            LOGGER.warn("Cannot set remote configs: WorldSession is null.", new RuntimeException());
            return;
        }
        if (ServerWorldSession.get() != null) {
            throw new IllegalStateException("Server configs should not be synchronized from remote on server.");
        }
        serverConfigs = remoteConfigs;
        LOGGER.info("Using remote server configs.");
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

            Map<String, Object> data = new LinkedHashMap<>();

            // Iterate through fields annotated with @ConfigCategory
            for (Field categoryField : INSTANCE.getClass().getDeclaredFields()) {
                if (categoryField.isAnnotationPresent(ConfigCategory.class)) {
                    categoryField.setAccessible(true);
                    Object categoryObject = categoryField.get(INSTANCE);
                    if (categoryObject instanceof Configs.PostLoader processor) {
                        processor.postLoad();
                    }
                    String fieldName = categoryField.getName();
                    Map<String, Object> categoryMap = saveConfigCategoryToMap(categoryObject);
                    data.put(fieldName, categoryMap);
                }
            }

            Yaml yaml = createYamlForDump();
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
            Yaml yaml = createYamlForLoad();
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
     * Called after server configs have been updated (saved, reloaded, or received from remote).
     * Refreshes all local world session caches and broadcasts to remote players if on server side.
     */
    @ClientAndServer
    public static void onUpdated() {
        WorldSession ws = WorldSession.get();
        if (ws != null) {
            ws.onConfigsUpdate(getClient(), getServer());
        }
        if (DevUtils.isClient()) {
            ClientWorldSession cws = ClientWorldSession.get();
            if (cws != null) {
                cws.onConfigsUpdate(getClient(), getServer());
            }
        }
        ServerWorldSession sws = ServerWorldSession.get();
        if (sws != null) {
            sws.onConfigsUpdate(getClient(), getServer());
            broadcastServerConfigs(sws.getServer());
        }
        LOGGER.info("Configs updated.");
    }

    /**
     * Broadcast current server configs to remote players.
     */
    @ClientAndServer
    private static void broadcastServerConfigs(MinecraftServer server) {
        String serverConfigsYaml = serializeConfigCategory(INSTANCE.getServer());

        if (server.isDedicatedServer()) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ServerNetManager.replyServerConfigs(player, serverConfigsYaml);
            }
            LOGGER.info("New server configs broadcasted to all players.");
        } else {
            if (ClientUtils.isSingleplayer()) {
                LOGGER.info("We are on a single-player server. No need to broadcast new configs.");
            } else {
                Player owner = ClientUtils.getClientPlayerCommon();
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (!Objects.equals(owner.getUUID(), player.getUUID())) {
                        ServerNetManager.replyServerConfigs(player, serverConfigsYaml);
                    }
                }
                LOGGER.info("New server configs broadcasted to remote players.");
            }
        }
    }

    // ==================== Config Entry Traversal ====================

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

    // ==================== Serialization/Deserialization Utilities ====================

    /**
     * Serialize a config category object to a YAML string.
     * This is used for sending configs over the network.
     *
     * @param configObject The config category object (e.g., ServerConfigs)
     * @return YAML string representation of the config
     */
    public static String serializeConfigCategory(Object configObject) {
        Map<String, Object> map = saveConfigCategoryToMap(configObject);
        Yaml yaml = createYamlForDump();
        try (StringWriter writer = new StringWriter()) {
            yaml.dump(map, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize config", e);
        }
    }

    /**
     * Deserialize a YAML string to a config category object.
     * This is used for receiving configs over the network.
     *
     * @param yamlString The YAML string representation
     * @param targetObject The target config object to populate (e.g., ServerConfigs)
     * @return true if deserialization succeeded, false otherwise
     */
    public static boolean deserializeConfigCategory(String yamlString, Object targetObject) {
        if (yamlString.length() > MAX_YAML_SIZE) {
            LOGGER.error("Config YAML string too large ({} bytes), max is {}", yamlString.length(), MAX_YAML_SIZE);
            return false;
        }
        try (StringReader reader = new StringReader(yamlString)) {
            Yaml yaml = createYamlForLoad();
            Map<?, ?> dataMap = yaml.load(reader);
            if (dataMap == null) {
                return false;
            }
            return loadConfigCategoryFromMap(dataMap, targetObject);
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize config", e);
            return false;
        }
    }

    private static Yaml createYamlForLoad() {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setCodePointLimit(MAX_YAML_SIZE);
        return new Yaml(new SafeConstructor(loaderOptions));
    }

    private static Yaml createYamlForDump() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        // Use custom representer to output empty arrays, maps as [], {}
        return new Yaml(new Representer(options) {
            @Override
            protected Node representScalar(Tag tag, String value, DumperOptions.ScalarStyle style) {
                // Force strings to double-quoted; leave numbers, booleans, etc. unquoted
                if (Tag.STR.equals(tag)) {
                    style = DumperOptions.ScalarStyle.DOUBLE_QUOTED;
                }
                return super.representScalar(tag, value, style);
            }

            @Override
            protected Node representSequence(Tag tag, Iterable<?> sequence,
                                             DumperOptions.FlowStyle flowStyle) {
                if (!sequence.iterator().hasNext()) {
                    // Output empty maps as flow style []
                    return super.representSequence(tag, sequence, DumperOptions.FlowStyle.FLOW);
                }
                return super.representSequence(tag, sequence, flowStyle);
            }

            @Override
            protected Node representMapping(Tag tag, Map<?, ?> mapping, DumperOptions.FlowStyle flowStyle) {
                if (mapping.isEmpty()) {
                    // Output empty maps as flow style {}
                    return super.representMapping(tag, mapping, DumperOptions.FlowStyle.FLOW);
                }
                return super.representMapping(tag, mapping, flowStyle);
            }
        });
    }

    /**
     * Convert config object to Map using reflection to avoid type tags in YAML.
     */
    private static Map<String, Object> saveConfigCategoryToMap(Object configObject) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Field field : configObject.getClass().getDeclaredFields()) {
            // Skip transient fields like skillCosts (they're handled separately)
            if (java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            if (field.isAnnotationPresent(ConfigEntry.class)) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(configObject);

                    // special cases
                    if (value instanceof Enum<?> e) {
                        value = e.name().toLowerCase();
                    } else if (value instanceof Set<?> s) {
                        value = s.stream().sorted().toList();
                    }

                    String yamlKey = StringUtils.camelToSnake(field.getName());
                    map.put(yamlKey, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return map;
    }

    /**
     * Convert maps (deserialized from YAML) to configs.
     */
    private static boolean loadConfigCategoryFromMap(Map<?, ?> dataMap, Object configObject) {
        boolean allGood = true;
        for (Map.Entry<?, ?> entry : dataMap.entrySet()) {
            try {
                String fieldName = StringUtils.snakeToLowerCamel((String) entry.getKey());
                Field field = configObject.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object convertedValue = Misc.convertNumber(entry.getValue(), fieldType);

                // special cases
                if (Enum.class.isAssignableFrom(fieldType)) {
                    convertedValue = Enum.valueOf(fieldType.asSubclass(Enum.class), ((String) convertedValue).toUpperCase());
                } else if (Set.class.isAssignableFrom(fieldType)) {
                    convertedValue = Set.copyOf((Collection<?>) convertedValue);
                }

                VarHandle.storeStoreFence();
                field.set(configObject, convertedValue);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException | RuntimeException e) {
                allGood = false;
                LOGGER.warn("Bad configuration entry {}: {}", entry.getKey(), entry.getValue(), e);
            }
        }

        // Post-process after loading
        if (configObject instanceof Configs.PostLoader processor) {
            processor.postLoad();
        }

        return allGood;
    }
}
