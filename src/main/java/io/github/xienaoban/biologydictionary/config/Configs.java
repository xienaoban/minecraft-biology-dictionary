package io.github.xienaoban.biologydictionary.config;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.annotation.Config;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigCategory;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigEntry;

/**
 * Main configuration data class for Biology Dictionary.
 * Uses annotations for automatic YAML serialization and Cloth Config generation.
 */
@Config(Lang.CONFIG_TITLE)
public final class Configs {
    @ConfigCategory(Lang.CONFIG_CATEGORY_SERVER)
    private final ServerConfigs server = new ServerConfigs();

    @ConfigCategory(Lang.CONFIG_CATEGORY_CLIENT)
    private final ClientConfigs client = new ClientConfigs();

    /**
     * Server-side configuration options.
     * These settings are used when running a local server or singleplayer.
     */
    public static class ServerConfigs {
        @ConfigEntry
        boolean enableDebugMode = false;
    }

    /**
     * Client-side configuration options.
     * These settings affect local rendering and behavior.
     */
    public static class ClientConfigs {
        @ConfigEntry
        FirstPersonShoulderEntityPosition firstPersonShoulderEntityPosition = FirstPersonShoulderEntityPosition.BOTTOM;
    }

    public ServerConfigs getServer() {
        return server;
    }

    public ClientConfigs getClient() {
        return client;
    }

    public enum FirstPersonShoulderEntityPosition {
        TOP, SIDES, BOTTOM,
    }
}
