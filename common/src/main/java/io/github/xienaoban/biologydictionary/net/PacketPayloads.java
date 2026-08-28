package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.net.payload.*;
import io.github.xienaoban.biologydictionary.platform.PlatformEntry;
import io.github.xienaoban.biologydictionary.platform.net.Packet;

import java.util.List;

public final class PacketPayloads {
    private PacketPayloads() {}

    @PlatformEntry
    public static final List<Entry<?>> ENTRIES = List.of(
            new Entry<>(SendCenteredMessagePacket.class, SendCenteredMessagePacket.FACTORY),
            new Entry<>(RequestServerConfigsPacket.class, RequestServerConfigsPacket.FACTORY),
            new Entry<>(ReplyServerConfigsPacket.class, ReplyServerConfigsPacket.FACTORY),
            new Entry<>(RequestBiologyDictionaryItemPacket.class, RequestBiologyDictionaryItemPacket.FACTORY),
            new Entry<>(RequestBiologyDictionaryDiscoveryFullPacket.class, RequestBiologyDictionaryDiscoveryFullPacket.FACTORY),
            new Entry<>(ReplyBiologyDictionaryDiscoveryFullPacket.class, ReplyBiologyDictionaryDiscoveryFullPacket.FACTORY),
            new Entry<>(RequestDiscoveryIncrementalPacket.class, RequestDiscoveryIncrementalPacket.FACTORY),
            new Entry<>(SendDiscoveryIncrementalPacket.class, SendDiscoveryIncrementalPacket.FACTORY),
            new Entry<>(SendSharedDiscoveryIncrementalPacket.class, SendSharedDiscoveryIncrementalPacket.FACTORY),
            new Entry<>(RequestEntityOverviewPacket.class, RequestEntityOverviewPacket.FACTORY),
            new Entry<>(ReplyEntityOverviewPacket.class, ReplyEntityOverviewPacket.FACTORY),
            new Entry<>(RequestEntityDataPacket.class, RequestEntityDataPacket.FACTORY),
            new Entry<>(ReplyEntityDataPacket.class, ReplyEntityDataPacket.FACTORY),
            new Entry<>(RequestCommonSkillPacket.class, RequestCommonSkillPacket.FACTORY),
            new Entry<>(RequestEntityTargetedSkillPacket.class, RequestEntityTargetedSkillPacket.FACTORY),
            new Entry<>(ReplyHighlightEntitiesPacket.class, ReplyHighlightEntitiesPacket.FACTORY),
            new Entry<>(ReplyInventoryStealingScreenPacket.class, ReplyInventoryStealingScreenPacket.FACTORY),
            new Entry<>(SendStealingDetectedPacket.class, SendStealingDetectedPacket.FACTORY),
            new Entry<>(RequestBeehiveInfoPacket.class, RequestBeehiveInfoPacket.FACTORY),
            new Entry<>(ReplyBeehiveInfoPacket.class, ReplyBeehiveInfoPacket.FACTORY)
    );

    public record Entry<T extends Packet>(Class<T> packetClass, Packet.Factory<T> factory) {}
}
