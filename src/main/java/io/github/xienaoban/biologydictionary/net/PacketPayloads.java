package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.net.payload.*;

import java.util.List;

public final class PacketPayloads {
    public static final List<Class<? extends Packet>> LIST = List.of(
            RequestBeehiveInfoPacket.class,
            RequestEntityDataPacket.class,
            RequestHandbookItemPacket.class,
            RequestEntityHighlightingPacket.class,
            RequestSpawnEggPacket.class,
            SendCenteredMessagePacket.class,
            SendBeehiveInfoPacket.class,
            SendEntityDataPacket.class,
            SendEntityOrientedSkillPacket.class,
            SendUpdatedEntityPropertiesOldPacket.class,
            SendEntityHighlightingPacket.class
    );
}
