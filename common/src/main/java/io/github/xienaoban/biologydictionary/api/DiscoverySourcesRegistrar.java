package io.github.xienaoban.biologydictionary.api;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;

/**
 * Registration handle for the discovery-source registry, passed to {@link DiscoverySourcesPlugin}.
 * The source's {@link DiscoverySource#id()} is its serialization identity and must be unique.
 */
public interface DiscoverySourcesRegistrar {
    void register(DiscoverySource source);
}
