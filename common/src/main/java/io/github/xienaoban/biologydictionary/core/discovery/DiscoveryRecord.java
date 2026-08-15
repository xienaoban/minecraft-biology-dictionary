package io.github.xienaoban.biologydictionary.core.discovery;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.UUID;

/**
 * Discovery record for a single entity type, belonging to a single player.
 * Presence in the map implies discovered; absence implies undiscovered.
 *
 * <p>Fields are never null; absent values use the NO_* sentinels defined here.
 * Construction goes through {@link #simple} / {@link #standard}; serialization
 * (NBT codec and network buffer) lives in {@link DiscoveryRecordSerializer},
 * because source strings resolve through the source registry.
 */
public record DiscoveryRecord(
        long firstDiscoveryTime,
        long firstDiscoveryTick,
        DiscoverySource source,
        Identifier dimension,
        Identifier biome,
        BlockPos position,
        Biome.Precipitation weather,
        UUID entityUUID,
        CompoundTag entityNbt
) {
    public static final long NO_TIME = -1L;
    public static final UUID NO_UUID = new UUID(-1, -1);
    public static final CompoundTag NO_NBT = new CompoundTag();
    public static final Identifier NO_ID = Identifier.withDefaultNamespace("unknown");

    /**
     * A record with only the source set; other fields get sentinel defaults.
     */
    public static DiscoveryRecord simple(DiscoverySource source) {
        return new DiscoveryRecord(NO_TIME, NO_TIME, source, NO_ID, NO_ID, BlockPos.ZERO,
                Biome.Precipitation.NONE, NO_UUID, NO_NBT);
    }

    /**
     * A standard record captured from a live entity right now.
     */
    public static DiscoveryRecord standard(long gameTick, Entity entity, DiscoverySource source) {
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        entity.saveWithoutId(output);
        return new DiscoveryRecord(
                System.currentTimeMillis(),
                gameTick,
                source,
                level.dimension().identifier(),
                level.getBiome(pos).unwrapKey().map(ResourceKey::identifier).orElse(NO_ID),
                pos,
                level.getBiome(pos).value().getPrecipitationAt(pos, level.getSeaLevel()),
                entity.getUUID(),
                output.buildResult()
        );
    }
}
