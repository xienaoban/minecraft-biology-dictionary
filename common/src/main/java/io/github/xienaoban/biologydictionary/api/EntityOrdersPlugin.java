package io.github.xienaoban.biologydictionary.api;

/**
 * Plugin for registering entity display order. Discovered and dispatched exactly once during
 * initialization, before the order registry freezes.
 */
public interface EntityOrdersPlugin {
    void registerEntityOrders(EntityOrdersRegistrar registrar);
}
