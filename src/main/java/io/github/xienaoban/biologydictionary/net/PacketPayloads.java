package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.net.payload.*;

import java.util.Arrays;
import java.util.List;

public final class PacketPayloads {
    public static final List<Class<? extends Packet>> LIST = Arrays.asList(
            SendCenteredMessagePacket.class,
            RequestEntityDataPacket.class,
            ReplyEntityDataPacket.class,
            RequestHandbookItemPacket.class,
            RequestCommonSkillPacket.class,
            RequestEntityOrientedSkillPacket.class,
            ReplyHighlightEntitiesPacket.class,
            ReplyInventoryStealingScreenPacket.class,
            RequestBeehiveInfoPacket.class,
            ReplyBeehiveInfoPacket.class
    );
}
