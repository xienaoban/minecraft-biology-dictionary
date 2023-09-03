package io.github.xienaoban.minecraft.biologydictionary.api;

@FunctionalInterface
public interface EntityPropertyWidgetRegistrar {
    void register(EntityPropertyWidgetRegistry<?> registry);
}
