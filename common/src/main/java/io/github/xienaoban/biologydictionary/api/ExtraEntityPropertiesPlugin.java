package io.github.xienaoban.biologydictionary.api;

/**
 * Plugin for registering extra entity properties. Discovered and dispatched exactly once during
 * initialization, before the property registry freezes.
 */
public interface ExtraEntityPropertiesPlugin {
    void registerExtraEntityProperties(ExtraEntityPropertiesRegistrar registrar);
}
