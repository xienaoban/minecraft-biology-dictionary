package io.github.xienaoban.biologydictionary.core.discovery;

import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Discovery record for a single entity type, belonging to a single player.
 * Presence in the map implies discovered; absence implies undiscovered.
 *
 * <p>Fields are never null; absent values use the NO_* sentinels defined here.
 * The discoverer made the discovery; a derived global record is identified only
 * by {@link #global} and always has an empty chain. The sharing chain exclusively
 * records explicit player-to-player sharing (tail = the sharer who gave it to the
 * current owner).
 * Construction goes through {@link #simple} / {@link #standard}; serialization
 * (NBT codec and network buffer) lives in {@link DiscoveryRecordSerializer}, because
 * source strings resolve through the source registry.
 */
public record DiscoveryRecord(
        UUID discoverer,
        long realTime,
        long gameTick,
        DiscoverySource source,
        Identifier dimension,
        Identifier biome,
        BlockPos position,
        Biome.Precipitation weather,
        UUID entityUUID,
        CompoundTag entityNbt,
        boolean global,
        List<UUID> shareChain
) {
    public static final long NO_TIME = -1L;
    public static final UUID NO_UUID = new UUID(-1, -1);
    public static final CompoundTag NO_NBT = new CompoundTag();
    public static final Identifier NO_ID = IdentifierUtils.mc("unknown");

    /**
     * Appearance-related entity NBT keys retained from the full save; everything else is dropped,
     * because the full NBT is bulky and the client never consumes the rest.
     */
    private static final Set<String> APPEARANCE_KEYS = Set.of(
            "Age", "MainGene", "HiddenGene", "variant", "Variant", "type", "Type",
            "RabbitType", "Color", "Sheared", "CollarColor", "Size", "size", "PuffState",
            "IsScreamingGoat", "HasLeftHorn", "HasRightHorn", "VillagerData", "CarpetColor"
    );

    /**
     * A record with only the source set; other fields get sentinel defaults.
     */
    public static DiscoveryRecord simple(DiscoverySource source) {
        return new DiscoveryRecord(NO_UUID, NO_TIME, NO_TIME, source, NO_ID, NO_ID, BlockPos.ZERO,
                Biome.Precipitation.NONE, NO_UUID, NO_NBT, false, List.of());
    }

    /**
     * A standard record captured from a live entity right now, discovered by the player.
     */
    public static DiscoveryRecord standard(ServerPlayer discoverer, Entity entity, DiscoverySource source) {
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        entity.saveWithoutId(output);
        return new DiscoveryRecord(
                discoverer.getUUID(),
                System.currentTimeMillis(),
                level.getGameTime(),
                source,
                level.dimension().identifier(),
                level.getBiome(pos).unwrapKey().map(ResourceKey::identifier).orElse(NO_ID),
                pos,
                level.getBiome(pos).value().getPrecipitationAt(pos, level.getSeaLevel()),
                entity.getUUID(),
                keepAppearanceOnly(output.buildResult()),
                false,
                List.of()
        );
    }

    private static CompoundTag keepAppearanceOnly(CompoundTag nbt) {
        CompoundTag result = new CompoundTag();
        for (String key : APPEARANCE_KEYS) {
            Tag tag = nbt.get(key);
            if (tag != null) {
                result.put(key, tag.copy());
            }
        }
        return result;
    }

    public DiscoveryRecord {
        Objects.requireNonNull(discoverer, "discoverer");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(biome, "biome");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(weather, "weather");
        Objects.requireNonNull(entityUUID, "entityUUID");
        Objects.requireNonNull(entityNbt, "entityNbt");
        Objects.requireNonNull(shareChain, "shareChain");
    }

    /**
     * The explicit sharer UUID who handed this record to its owner (tail of the
     * chain), or {@code null} when no active sharing occurred.
     */
    public UUID sharer() {
        return shareChain.isEmpty() ? null : shareChain.getLast();
    }

    /**
     * This record presented as a derived global-view record: {@code global=true} and an
     * empty share chain. Global sharing never reads or writes the chain.
     */
    public DiscoveryRecord asGlobal() {
        return new DiscoveryRecord(discoverer, realTime, gameTick, source, dimension, biome, position, weather,
                entityUUID, entityNbt, true, List.of());
    }
}
