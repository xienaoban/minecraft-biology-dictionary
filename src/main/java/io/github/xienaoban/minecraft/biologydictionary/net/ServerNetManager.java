package io.github.xienaoban.minecraft.biologydictionary.net;

import io.github.xienaoban.minecraft.biologydictionary.platform.net.ServerNetApi;

public final class ServerNetManager {
    public static void init() {
        PacketPayloads.LIST.forEach(ServerNetApi::register);
    }
}
