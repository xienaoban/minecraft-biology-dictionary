package io.github.xienaoban.biologydictionary.api.plugin;

import io.github.xienaoban.biologydictionary.api.ClientDiscoveryApi;
import io.github.xienaoban.biologydictionary.api.ServerDiscoveryApi;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;

/**
 * Plugin for registering custom {@link DiscoverySource}s.
 * Discovered and dispatched exactly once during initialization.
 *
 * <p>A registered source is effective only under the Biology Dictionary discovery strategy; the
 * other two strategies ignore plugin sources. The plugin holds the returned source and fires it
 * via {@link ServerDiscoveryApi#recordDiscovery} / {@link ClientDiscoveryApi#recordDiscovery}
 * when its own trigger condition is met.
 */
public interface DiscoverySourcesPlugin {
    void registerDiscoverySources(DiscoverySourcesPlugin.Registrar registrar);

    interface Registrar {
        void register(DiscoverySource source);
    }
}
