package io.github.xienaoban.biologydictionary.core.discovery;

/**
 * Discovery record for a single entity type, belonging to a single player.
 *
 * @param firstDiscoveryTime epoch millis, 0 if not discovered
 * @param firstDiscoveryTick game time tick, 0 if not discovered
 */
public record DiscoveryRecord(boolean discovered, long firstDiscoveryTime, long firstDiscoveryTick) {
    private static final long NO_TIME = -1L;

    public DiscoveryRecord(boolean discovered) {
        this(discovered, NO_TIME, NO_TIME);
    }

    /**
     * Create a discovered record with the current real time and given game tick.
     */
    public static DiscoveryRecord discoveredNow(long gameTick) {
        return new DiscoveryRecord(true, System.currentTimeMillis(), gameTick);
    }

    /**
     * Undiscovered record singleton.
     */
    public static final DiscoveryRecord UNDISCOVERED = new DiscoveryRecord(false);
}
