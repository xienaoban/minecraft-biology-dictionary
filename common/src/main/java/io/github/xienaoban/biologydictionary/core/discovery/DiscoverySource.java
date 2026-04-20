package io.github.xienaoban.biologydictionary.core.discovery;

/**
 * How a player discovered an entity.
 * Aligned with {@link DiscoveryEventListener} methods.
 */
public enum DiscoverySource {
    ENTITY_DETAIL_SCREEN,
    HIGHLIGHT,
    TELESCOPE_OBSERVE,
    KILL,
    ATTACK,
    INTERACT,
    FEED,
    TAME,
    UNKNOWN
}
