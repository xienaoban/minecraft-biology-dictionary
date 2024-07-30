package io.github.xienaoban.minecraft.biologydictionary.net;

import io.github.xienaoban.minecraft.biologydictionary.net.payloads.RequestEntityDataPacketPayload;
import io.github.xienaoban.minecraft.biologydictionary.net.payloads.RequestHandbookItemPacketPayload;
import io.github.xienaoban.minecraft.biologydictionary.net.payloads.SendEntityDataPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public final class PacketPayloads {
    public static final List<Class<? extends CustomPacketPayload>> LIST = List.of(
            RequestEntityDataPacketPayload.class,
            RequestHandbookItemPacketPayload.class,
            SendEntityDataPacketPayload.class
    );
}
