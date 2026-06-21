package io.github.xienaoban.biologydictionary.config;

/**
 * Callback invoked when configs are updated (saved, reloaded, or received from remote).
 */
@FunctionalInterface
public interface ConfigsUpdateCallback {
    void onConfigsUpdate(Configs.ClientConfigs clientConfigs, Configs.ServerConfigs serverConfigs);
}
