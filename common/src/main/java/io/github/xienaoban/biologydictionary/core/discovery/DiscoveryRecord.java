package io.github.xienaoban.biologydictionary.core.discovery;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.UUID;

/**
 * Discovery record for a single entity type, belonging to a single player.
 * Presence in the map implies discovered; absence implies undiscovered.
 */
public record DiscoveryRecord(
    long firstDiscoveryTime,
    long firstDiscoveryTick,
    DiscoverySource source,
    ResourceLocation dimension,
    ResourceLocation biome,
    BlockPos position,
    Biome.Precipitation weather,
    UUID entityUUID,
    CompoundTag entityNbt
) {
    private static final long NO_TIME = -1L;
    private static final UUID NO_UUID = new UUID(-1, -1);
    private static final CompoundTag NO_NBT = new CompoundTag();
    private static final ResourceLocation NO_ID = ResourceLocation.withDefaultNamespace("unknown");

    public DiscoveryRecord() {
        this(DiscoverySource.UNKNOWN);
    }

    public DiscoveryRecord(DiscoverySource source) {
        this(NO_TIME, NO_TIME, source, NO_ID, NO_ID, BlockPos.ZERO, Biome.Precipitation.NONE, NO_UUID, NO_NBT);
    }

    public static DiscoveryRecord readFromBuf(FriendlyByteBuf buf) {
        long time = buf.readLong();
        long tick = buf.readLong();
        DiscoverySource source = DiscoverySource.valueOf(buf.readUtf());
        String dimStr = buf.readUtf();
        ResourceLocation dimension = dimStr.isEmpty() ? null : ResourceLocation.tryParse(dimStr);
        String bioStr = buf.readUtf();
        ResourceLocation biome = bioStr.isEmpty() ? null : ResourceLocation.tryParse(bioStr);
        BlockPos position = buf.readBlockPos();
        Biome.Precipitation weather = Biome.Precipitation.valueOf(buf.readUtf());
        UUID entityUUID = buf.readUUID();
        CompoundTag entityNbt = buf.readNbt();
        return new DiscoveryRecord(time, tick, source, dimension, biome, position, weather, entityUUID, entityNbt);
    }

    public void writeToBuf(FriendlyByteBuf buf) {
        buf.writeLong(firstDiscoveryTime);
        buf.writeLong(firstDiscoveryTick);
        buf.writeUtf(source.name());
        buf.writeUtf(dimension != null ? dimension.toString() : "");
        buf.writeUtf(biome != null ? biome.toString() : "");
        buf.writeBlockPos(position);
        buf.writeUtf(weather.name());
        buf.writeUUID(entityUUID);
        buf.writeNbt(entityNbt);
    }

    public static DiscoveryRecord discoveredNow(long gameTick, Entity entity, DiscoverySource source) {
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        CompoundTag entityNbt = new CompoundTag();
        entity.saveWithoutId(entityNbt);
        return new DiscoveryRecord(
            System.currentTimeMillis(),
            gameTick,
            source,
            level.dimension().location(),
            level.getBiome(pos).unwrapKey().map(ResourceKey::location).orElse(NO_ID),
            pos,
            level.getBiome(pos).value().getPrecipitationAt(pos),
            entity.getUUID(),
            entityNbt
        );
    }
}
