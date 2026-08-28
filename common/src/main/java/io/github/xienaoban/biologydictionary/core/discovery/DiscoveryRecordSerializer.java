package io.github.xienaoban.biologydictionary.core.discovery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Serialization for {@link DiscoveryRecord}: NBT codec and network buffer.
 * Lives in core because source strings round-trip through the source registry
 * ({@link DiscoverySources}), which is core-only. Construction helpers live on
 * the record itself.
 */
public final class DiscoveryRecordSerializer {
    private DiscoveryRecordSerializer() {}

    private static final Codec<DiscoverySource> SOURCE_CODEC =
            Codec.STRING.xmap(DiscoverySources::parseSource, source -> IdentifierUtils.toString(source.id()));
    private static final Codec<Biome.Precipitation> WEATHER_CODEC =
            Codec.STRING.xmap(DiscoveryRecordSerializer::parsePrecipitation, Biome.Precipitation::getSerializedName);

    private static final Codec<DiscoveryRecord.DiscoveryShareLink> SHARE_LINK_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    UUIDUtil.CODEC.optionalFieldOf("sharer", DiscoveryRecord.NO_UUID)
                            .forGetter(DiscoveryRecord.DiscoveryShareLink::sharer),
                    Codec.STRING.optionalFieldOf("sharer_name", DiscoveryRecord.NO_NAME)
                            .forGetter(DiscoveryRecord.DiscoveryShareLink::sharerName)
            ).apply(instance, DiscoveryRecord.DiscoveryShareLink::new));

    public static final Codec<DiscoveryRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("time", DiscoveryRecord.NO_TIME).forGetter(DiscoveryRecord::firstDiscoveryTime),
            Codec.LONG.optionalFieldOf("tick", DiscoveryRecord.NO_TIME).forGetter(DiscoveryRecord::firstDiscoveryTick),
            SOURCE_CODEC.optionalFieldOf("source", DiscoverySources.UNKNOWN).forGetter(DiscoveryRecord::source),
            IdentifierUtils.codec().optionalFieldOf("dimension", DiscoveryRecord.NO_ID).forGetter(DiscoveryRecord::dimension),
            IdentifierUtils.codec().optionalFieldOf("biome", DiscoveryRecord.NO_ID).forGetter(DiscoveryRecord::biome),
            BlockPos.CODEC.optionalFieldOf("position", BlockPos.ZERO).forGetter(DiscoveryRecord::position),
            WEATHER_CODEC.optionalFieldOf("weather", Biome.Precipitation.NONE).forGetter(DiscoveryRecord::weather),
            UUIDUtil.CODEC.optionalFieldOf("entity_uuid", DiscoveryRecord.NO_UUID).forGetter(DiscoveryRecord::entityUUID),
            CompoundTag.CODEC.optionalFieldOf("entity_nbt", DiscoveryRecord.NO_NBT).forGetter(DiscoveryRecord::entityNbt),
            UUIDUtil.CODEC.optionalFieldOf("discoverer", DiscoveryRecord.NO_UUID).forGetter(DiscoveryRecord::discoverer),
            Codec.STRING.optionalFieldOf("discoverer_name", DiscoveryRecord.NO_NAME).forGetter(DiscoveryRecord::discovererName),
            Codec.list(SHARE_LINK_CODEC).optionalFieldOf("share_chain", List.of())
                    .forGetter(DiscoveryRecord::shareChain),
            Codec.BOOL.optionalFieldOf("global", false).forGetter(DiscoveryRecord::global)
    ).apply(instance, DiscoveryRecord::new));

    public static DiscoveryRecord readFromBuf(FriendlyByteBuf buf) {
        long time = buf.readLong();
        long tick = buf.readLong();
        DiscoverySource source = DiscoverySources.parseSource(buf.readUtf());
        String dimStr = buf.readUtf();
        Identifier dimension = dimStr.isEmpty() ? DiscoveryRecord.NO_ID : IdentifierUtils.fromString(dimStr);
        String bioStr = buf.readUtf();
        Identifier biome = bioStr.isEmpty() ? DiscoveryRecord.NO_ID : IdentifierUtils.fromString(bioStr);
        BlockPos position = buf.readBlockPos();
        Biome.Precipitation weather = parsePrecipitation(buf.readUtf());
        UUID entityUUID = buf.readUUID();
        CompoundTag entityNbt = buf.readNbt();
        UUID discoverer = buf.readUUID();
        String discovererName = buf.readUtf();
        int chainSize = buf.readVarInt();
        List<DiscoveryRecord.DiscoveryShareLink> shareChain = new ArrayList<>(chainSize);
        for (int i = 0; i < chainSize; i++) {
            shareChain.add(new DiscoveryRecord.DiscoveryShareLink(buf.readUUID(), buf.readUtf()));
        }
        boolean global = buf.readBoolean();
        return new DiscoveryRecord(time, tick, source, dimension, biome, position, weather, entityUUID,
                entityNbt != null ? entityNbt : DiscoveryRecord.NO_NBT, discoverer, discovererName, shareChain, global);
    }

    public static void writeToBuf(FriendlyByteBuf buf, DiscoveryRecord record) {
        buf.writeLong(record.firstDiscoveryTime());
        buf.writeLong(record.firstDiscoveryTick());
        IdentifierUtils.toBuf(buf, record.source().id());
        IdentifierUtils.toBuf(buf, record.dimension());
        IdentifierUtils.toBuf(buf, record.biome());
        buf.writeBlockPos(record.position());
        buf.writeUtf(record.weather().getSerializedName());
        buf.writeUUID(record.entityUUID());
        // The entity NBT is unused on the client for now, so send an empty tag to keep the payload small.
        buf.writeNbt(DiscoveryRecord.NO_NBT);
        buf.writeUUID(record.discoverer());
        buf.writeUtf(record.discovererName());
        buf.writeVarInt(record.shareChain().size());
        for (DiscoveryRecord.DiscoveryShareLink link : record.shareChain()) {
            buf.writeUUID(link.sharer());
            buf.writeUtf(link.sharerName());
        }
        buf.writeBoolean(record.global());
    }

    /**
     * Lenient parse of a serialized precipitation: prefers the stable serialized name
     * ({@link Biome.Precipitation#getSerializedName()}), falls back to the legacy
     * {@link Enum#name()} format, then to {@link Biome.Precipitation#NONE}.
     */
    private static Biome.Precipitation parsePrecipitation(String value) {
        for (Biome.Precipitation precipitation : Biome.Precipitation.values()) {
            if (precipitation.getSerializedName().equals(value)) { return precipitation; }
        }
        try {
            return Biome.Precipitation.valueOf(value);
        } catch (IllegalArgumentException e) {
            return Biome.Precipitation.NONE;
        }
    }
}
