package io.github.xienaoban.minecraft.biologydictionary.api;

@FunctionalInterface
public interface EntityWidgetRegistrar {
    void register(EntityWidgetRegistry<?> registry);
}
