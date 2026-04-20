package io.github.xienaoban.biologydictionary.core.discovery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Discovery record for a single entity type, belonging to a single player.
 *
 * @param firstDiscoveryTime epoch millis, 0 if not discovered
 * @param firstDiscoveryTick game time tick, 0 if not discovered
 */
public record DiscoveryRecord(boolean discovered, long firstDiscoveryTime, long firstDiscoveryTick) {
    public static final Codec<DiscoveryRecord> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.BOOL.fieldOf("discovered").forGetter(DiscoveryRecord::discovered),
        Codec.LONG.fieldOf("time").forGetter(DiscoveryRecord::firstDiscoveryTime),
        Codec.LONG.fieldOf("tick").forGetter(DiscoveryRecord::firstDiscoveryTick)
    ).apply(i, DiscoveryRecord::new));
    /**
     * Undiscovered record singleton.
     */
    public static final DiscoveryRecord UNDISCOVERED = new DiscoveryRecord(false);

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
}
