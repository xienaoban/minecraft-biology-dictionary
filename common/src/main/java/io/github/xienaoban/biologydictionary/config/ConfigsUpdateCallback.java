package io.github.xienaoban.biologydictionary.config;

@FunctionalInterface
public interface ConfigsUpdateCallback {
    void onConfigsUpdate(Configs.ClientConfigs clientConfigs, Configs.ServerConfigs serverConfigs);
}
