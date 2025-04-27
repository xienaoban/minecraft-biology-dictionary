package io.github.xienaoban.minecraft.biologydictionary.net;

import io.github.xienaoban.minecraft.biologydictionary.net.payloads.*;
import io.github.xienaoban.minecraft.biologydictionary.common.net.PacketPayload;

import java.util.List;

public final class PacketPayloads {
    public static final List<Class<? extends PacketPayload>> LIST = List.of(
            RequestBeehiveInfoPacketPayload.class,
            RequestEntityDataPacketPayload.class,
            RequestHandbookItemPacketPayload.class,
            SendBeehiveInfoPacketPayload.class,
            SendEntityDataPacketPayload.class
    );
}
