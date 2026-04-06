package io.github.xienaoban.biologydictionary.core.discovery;

/**
 * Discovery record for a single entity type, belonging to a single player.
 */
public class DiscoveryRecord {
    private final boolean discovered;
    private final long firstDiscoveryTime; // epoch millis, 0 if not discovered
    private final long firstDiscoveryTick; // game time tick, 0 if not discovered

    private static final long NO_TIME = -1L;

    public DiscoveryRecord(boolean discovered) {
        this(discovered, NO_TIME, NO_TIME);
    }

    public DiscoveryRecord(boolean discovered, long firstDiscoveryTime, long firstDiscoveryTick) {
        this.discovered = discovered;
        this.firstDiscoveryTime = firstDiscoveryTime;
        this.firstDiscoveryTick = firstDiscoveryTick;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public long getFirstDiscoveryTime() {
        return firstDiscoveryTime;
    }

    public long getFirstDiscoveryTick() {
        return firstDiscoveryTick;
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
