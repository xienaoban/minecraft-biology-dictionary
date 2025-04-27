package io.github.xienaoban.minecraft.biologydictionary.net;

import io.github.xienaoban.minecraft.biologydictionary.common.net.ServerNetApi;

public final class ServerNetManager {
    public static void init() {
        PacketPayloads.LIST.forEach(ServerNetApi::register);
    }
}
