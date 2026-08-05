package io.github.xienaoban.biologydictionary.api;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;

/**
 * Client-only plugin for registering entity property widgets. Discovered and dispatched exactly once
 * during client initialization, before the widget registry freezes.
 */
@ClientOnly
public interface EntityPropertyWidgetsPlugin {
    void registerEntityPropertyWidgets(EntityPropertyWidgetsRegistrar registrar);
}
