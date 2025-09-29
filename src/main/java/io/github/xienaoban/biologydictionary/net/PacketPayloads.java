package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.net.payload.*;

import java.util.List;

public final class PacketPayloads {
    public static final List<Class<? extends Packet>> LIST = List.of(
            RequestBeehiveInfoPacket.class,
            RequestEntityDataPacket.class,
            RequestHandbookItemPacket.class,
            SendCenteredMessagePacket.class,
            ReplyBeehiveInfoPacket.class,
            ReplyEntityDataPacket.class,
            RequestCommonSkillPacket.class,
            RequestEntityOrientedSkillPacket.class,
            SendUpdatedEntityPropertiesOldPacket.class,
            ReplyHighlightEntitiesPacket.class
    );
}
