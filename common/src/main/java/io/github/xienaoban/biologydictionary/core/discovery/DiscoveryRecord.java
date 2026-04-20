package io.github.xienaoban.biologydictionary.core.discovery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Discovery record for a single entity type, belonging to a single player.
 * Presence in the map implies discovered; absence implies undiscovered.
 *
 * @param firstDiscoveryTime epoch millis
 * @param firstDiscoveryTick game time tick
 */
public record DiscoveryRecord(long firstDiscoveryTime, long firstDiscoveryTick) {
    private static final long NO_TIME = -1L;

    public static final Codec<DiscoveryRecord> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.LONG.fieldOf("time").forGetter(DiscoveryRecord::firstDiscoveryTime),
        Codec.LONG.fieldOf("tick").forGetter(DiscoveryRecord::firstDiscoveryTick)
    ).apply(i, DiscoveryRecord::new));

    public DiscoveryRecord() {
        this(NO_TIME, NO_TIME);
    }

    public static DiscoveryRecord discoveredNow(long gameTick) {
        return new DiscoveryRecord(System.currentTimeMillis(), gameTick);
    }
}
