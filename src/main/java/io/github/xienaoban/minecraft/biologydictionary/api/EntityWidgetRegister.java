package io.github.xienaoban.minecraft.biologydictionary.api;

import io.github.xienaoban.minecraft.biologydictionary.core.registry.EntityWidgetRegistryManager;

@FunctionalInterface
public interface EntityWidgetRegister {
    void registerTo(EntityWidgetRegistryManager manager);
}
