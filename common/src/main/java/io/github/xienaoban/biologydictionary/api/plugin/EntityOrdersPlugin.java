package io.github.xienaoban.biologydictionary.api.plugin;

import net.minecraft.world.entity.EntityType;

/**
 * Plugin for registering entity display order. Discovered and dispatched exactly once during
 * initialization, before the order registry freezes.
 */
public interface EntityOrdersPlugin {
    void registerEntityOrders(EntityOrdersPlugin.Registrar registrar);

    interface Registrar {
        void register(EntityType<?> entityType);
    }
}
