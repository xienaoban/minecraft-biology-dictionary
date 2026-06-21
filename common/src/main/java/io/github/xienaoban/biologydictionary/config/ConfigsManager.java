package io.github.xienaoban.biologydictionary.config;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigCategory;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigEntry;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
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

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public final class ConfigsManager {
    private static final int MAX_YAML_SIZE = 64 * 1024;

    private static final Configs INSTANCE = new Configs();
    private static final Configs.ClientConfigs clientConfigs = INSTANCE.getClient();
    private static volatile Configs.ServerConfigs serverConfigs = INSTANCE.getServer();

    private ConfigsManager() {}

    public static Configs getInstance() {
        return INSTANCE;
    }

    public static Configs.ClientConfigs getClient() {
        return clientConfigs;
    }

    public static Configs.ServerConfigs getServer() {
        return serverConfigs;
    }

    public static void setLocalServerConfigs() {
        serverConfigs = INSTANCE.getServer();
        LOGGER.info("Using local server configs.");
    }

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

    public static void save() {
        Path configPath = getConfigPath();
        try {
            Files.createDirectories(configPath.getParent());

            Map<String, Object> data = new LinkedHashMap<>();
            for (Field categoryField : INSTANCE.getClass().getDeclaredFields()) {
                if (categoryField.isAnnotationPresent(ConfigCategory.class)) {
                    categoryField.setAccessible(true);
                    Object categoryObject = categoryField.get(INSTANCE);
                    if (categoryObject instanceof Configs.PostLoader postLoader) {
                        postLoader.postLoad();
                    }
                    data.put(categoryField.getName(), saveConfigCategoryToMap(categoryObject));
                }
            }

            try (FileWriter writer = new FileWriter(configPath.toFile())) {
                createYamlForDump().dump(data, writer);
            }
            LOGGER.info("Configuration saved to {}", configPath.toAbsolutePath());
        } catch (IOException | IllegalAccessException e) {
            LOGGER.error("Failed to save configuration", e);
        }
    }

    public static void load() {
        Path configPath = getConfigPath();
        if (!Files.exists(configPath)) {
            save();
            return;
        }

        try (FileInputStream input = new FileInputStream(configPath.toFile())) {
            Map<String, Object> data = createYamlForLoad().load(input);
            boolean allGood = true;
            if (data != null) {
                for (Field categoryField : INSTANCE.getClass().getDeclaredFields()) {
                    if (categoryField.isAnnotationPresent(ConfigCategory.class)) {
                        Map<?, ?> categoryData = (Map<?, ?>) data.get(categoryField.getName());
                        if (categoryData != null) {
                            categoryField.setAccessible(true);
                            allGood &= loadConfigCategoryFromMap(categoryData, categoryField.get(INSTANCE));
                        }
                    }
                }
            }
            LOGGER.info("Configuration loaded from {}", configPath);
            if (!allGood) {
                LOGGER.warn("Not all configuration entries are legal. Refreshing the configuration file.");
                save();
            }
        } catch (IOException | IllegalAccessException | RuntimeException e) {
            LOGGER.error("Failed to load configuration", e);
            save();
        }
    }

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
            sws.getDiscoveryManager().onConfigsUpdate(getClient(), getServer());
            broadcastServerConfigs(sws.getServer());
        }
        LOGGER.info("Configs updated.");
    }

    private static void broadcastServerConfigs(MinecraftServer server) {
        String serverConfigsYaml = serializeConfigCategory(INSTANCE.getServer());
        if (server.isDedicatedServer()) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ServerNetManager.replyServerConfigs(player, serverConfigsYaml);
            }
            LOGGER.info("New server configs broadcasted to all players.");
            return;
        }

        if (ClientUtils.isSingleplayer()) {
            LOGGER.info("We are on a single-player server. No need to broadcast new configs.");
            return;
        }

        Player owner = ClientUtils.getClientPlayerCommon();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (owner == null || !Objects.equals(owner.getUUID(), player.getUUID())) {
                ServerNetManager.replyServerConfigs(player, serverConfigsYaml);
            }
        }
        LOGGER.info("New server configs broadcasted to remote players.");
    }

    private static Path getConfigPath() {
        return DevUtils.getConfigDir().resolve(Lang.CONFIG_FILE);
    }

    public static void forEachConfigEntryInCategory(Object categoryObject, Consumer<ConfigEntryInfo> consumer) {
        for (Field entryField : categoryObject.getClass().getDeclaredFields()) {
            if (entryField.isAnnotationPresent(ConfigEntry.class)) {
                consumer.accept(new ConfigEntryInfo(entryField, entryField.getAnnotation(ConfigEntry.class), categoryObject));
            }
        }
    }

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

    public static String serializeConfigCategory(Object configObject) {
        Map<String, Object> map = saveConfigCategoryToMap(configObject);
        try (StringWriter writer = new StringWriter()) {
            createYamlForDump().dump(map, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize config", e);
        }
    }

    public static boolean deserializeConfigCategory(String yamlString, Object targetObject) {
        if (yamlString.length() > MAX_YAML_SIZE) {
            LOGGER.error("Config YAML string too large ({} bytes), max is {}", yamlString.length(), MAX_YAML_SIZE);
            return false;
        }
        try (StringReader reader = new StringReader(yamlString)) {
            Map<?, ?> dataMap = createYamlForLoad().load(reader);
            return dataMap != null && loadConfigCategoryFromMap(dataMap, targetObject);
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
        return new Yaml(new Representer(options) {
            @Override
            protected Node representScalar(Tag tag, String value, DumperOptions.ScalarStyle style) {
                if (Tag.STR.equals(tag)) {
                    style = DumperOptions.ScalarStyle.DOUBLE_QUOTED;
                }
                return super.representScalar(tag, value, style);
            }

            @Override
            protected Node representSequence(Tag tag, Iterable<?> sequence, DumperOptions.FlowStyle flowStyle) {
                if (!sequence.iterator().hasNext()) {
                    return super.representSequence(tag, sequence, DumperOptions.FlowStyle.FLOW);
                }
                return super.representSequence(tag, sequence, flowStyle);
            }

            @Override
            protected Node representMapping(Tag tag, Map<?, ?> mapping, DumperOptions.FlowStyle flowStyle) {
                if (mapping.isEmpty()) {
                    return super.representMapping(tag, mapping, DumperOptions.FlowStyle.FLOW);
                }
                return super.representMapping(tag, mapping, flowStyle);
            }
        });
    }

    private static Map<String, Object> saveConfigCategoryToMap(Object configObject) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Field field : configObject.getClass().getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()) || !field.isAnnotationPresent(ConfigEntry.class)) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(configObject);
                if (value instanceof Enum<?> enumValue) {
                    value = enumValue.name().toLowerCase();
                } else if (value instanceof Set<?> set) {
                    value = set.stream().sorted().toList();
                }
                map.put(StringUtils.camelToSnake(field.getName()), value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    private static boolean loadConfigCategoryFromMap(Map<?, ?> dataMap, Object configObject) {
        boolean allGood = true;
        for (Map.Entry<?, ?> entry : dataMap.entrySet()) {
            try {
                String fieldName = StringUtils.snakeToLowerCamel((String) entry.getKey());
                Field field = configObject.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object convertedValue = Misc.convertNumber(entry.getValue(), fieldType);
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
        if (configObject instanceof Configs.PostLoader postLoader) {
            postLoader.postLoad();
        }
        return allGood;
    }
}
