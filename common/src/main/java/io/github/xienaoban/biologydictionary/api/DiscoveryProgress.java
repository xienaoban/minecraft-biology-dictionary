package io.github.xienaoban.biologydictionary.api;

/**
 * Snapshot of a player's discovery progress.
 *
 * @param discovered how many entity types the player has discovered
 * @param total      how many entity types are trackable (non-blacklisted living entities)
 */
public record DiscoveryProgress(int discovered, int total) {
}
