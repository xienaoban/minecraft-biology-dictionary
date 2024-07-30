package io.github.xienaoban.minecraft.biologydictionary.net;

import io.github.xienaoban.minecraft.biologydictionary.net.payloads.RequestEntityDataPacketPayload;
import io.github.xienaoban.minecraft.biologydictionary.net.payloads.RequestHandbookItemPacketPayload;
import io.github.xienaoban.minecraft.biologydictionary.net.payloads.SendEntityDataPacketPayload;
import io.github.xienaoban.minecraft.biologydictionary.platform.net.PacketPayload;

import java.util.List;

public final class PacketPayloads {
    public static final List<Class<? extends PacketPayload>> LIST = List.of(
            RequestEntityDataPacketPayload.class,
            RequestHandbookItemPacketPayload.class,
            SendEntityDataPacketPayload.class
    );
}
