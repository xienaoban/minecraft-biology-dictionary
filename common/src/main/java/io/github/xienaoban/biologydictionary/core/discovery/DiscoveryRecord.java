package io.github.xienaoban.biologydictionary.core.discovery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.UUID;

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
	private static final long NO_TIME = -1L;
	private static final UUID NO_UUID = new UUID(-1, -1);
	private static final CompoundTag NO_NBT = new CompoundTag();
	private static final Identifier NO_ID = Identifier.withDefaultNamespace("unknown");

	private static final Codec<DiscoverySource> SOURCE_CODEC = Codec.STRING.xmap(DiscoverySource::valueOf, DiscoverySource::name);
	private static final Codec<Biome.Precipitation> WEATHER_CODEC = Codec.STRING.xmap(Biome.Precipitation::valueOf, Biome.Precipitation::name);

	public static final Codec<DiscoveryRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.optionalFieldOf("time", NO_TIME).forGetter(DiscoveryRecord::firstDiscoveryTime),
			Codec.LONG.optionalFieldOf("tick", NO_TIME).forGetter(DiscoveryRecord::firstDiscoveryTick),
			SOURCE_CODEC.optionalFieldOf("source", DiscoverySource.UNKNOWN).forGetter(DiscoveryRecord::source),
			Identifier.CODEC.optionalFieldOf("dimension", NO_ID).forGetter(DiscoveryRecord::dimension),
			Identifier.CODEC.optionalFieldOf("biome", NO_ID).forGetter(DiscoveryRecord::biome),
			BlockPos.CODEC.optionalFieldOf("position", BlockPos.ZERO).forGetter(DiscoveryRecord::position),
			WEATHER_CODEC.optionalFieldOf("weather", Biome.Precipitation.NONE).forGetter(DiscoveryRecord::weather),
			UUIDUtil.CODEC.optionalFieldOf("entity_uuid", NO_UUID).forGetter(DiscoveryRecord::entityUUID),
			CompoundTag.CODEC.optionalFieldOf("entity_nbt", NO_NBT).forGetter(DiscoveryRecord::entityNbt)
	).apply(instance, DiscoveryRecord::new));

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
		String dimensionString = buf.readUtf();
		Identifier dimension = dimensionString.isEmpty() ? null : Identifier.tryParse(dimensionString);
		String biomeString = buf.readUtf();
		Identifier biome = biomeString.isEmpty() ? null : Identifier.tryParse(biomeString);
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
