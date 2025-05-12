package io.github.xienaoban.minecraft.biologydictionary.net;

import io.github.xienaoban.minecraft.biologydictionary.common.net.Packet;
import io.github.xienaoban.minecraft.biologydictionary.net.payloads.*;

import java.util.List;

public final class PacketPayloads {
    public static final List<Class<? extends Packet>> LIST = List.of(
            RequestBeehiveInfoPacket.class,
            RequestEntityDataPacket.class,
            RequestHandbookItemPacket.class,
            SendBeehiveInfoPacket.class,
            SendEntityDataPacket.class,
            SendUpdatedEntityPropertiesPacket.class
    );
}
