package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.net.payload.*;
import io.github.xienaoban.biologydictionary.platform.net.Packet;

public final class PacketPayloads {

    private PacketPayloads() {}

    public static void registerBuiltIn(Registrar registrar) {
        registrar.register(SendCenteredMessagePacket.class, SendCenteredMessagePacket.FACTORY);
        registrar.register(RequestEntityOverviewPacket.class, RequestEntityOverviewPacket.FACTORY);
        registrar.register(ReplyEntityOverviewPacket.class, ReplyEntityOverviewPacket.FACTORY);
        registrar.register(RequestEntityDataPacket.class, RequestEntityDataPacket.FACTORY);
        registrar.register(ReplyEntityDataPacket.class, ReplyEntityDataPacket.FACTORY);
        registrar.register(RequestBiologyDictionaryItemPacket.class, RequestBiologyDictionaryItemPacket.FACTORY);
        registrar.register(RequestCommonSkillPacket.class, RequestCommonSkillPacket.FACTORY);
        registrar.register(RequestEntityTargetedSkillPacket.class, RequestEntityTargetedSkillPacket.FACTORY);
        registrar.register(ReplyHighlightEntitiesPacket.class, ReplyHighlightEntitiesPacket.FACTORY);
        registrar.register(ReplyInventoryStealingScreenPacket.class, ReplyInventoryStealingScreenPacket.FACTORY);
        registrar.register(SendStealingDetectedPacket.class, SendStealingDetectedPacket.FACTORY);
        registrar.register(RequestBeehiveInfoPacket.class, RequestBeehiveInfoPacket.FACTORY);
        registrar.register(ReplyBeehiveInfoPacket.class, ReplyBeehiveInfoPacket.FACTORY);
        registrar.register(RequestFullSyncPacket.class, RequestFullSyncPacket.FACTORY);
        registrar.register(ReplyFullSyncPacket.class, ReplyFullSyncPacket.FACTORY);
        registrar.register(ReplyDiscoveryUpdatePacket.class, ReplyDiscoveryUpdatePacket.FACTORY);
    }

    @FunctionalInterface
    public interface Registrar {
        <T extends Packet> void register(Class<T> packetClass, Packet.Factory<T> factory);
    }
}
