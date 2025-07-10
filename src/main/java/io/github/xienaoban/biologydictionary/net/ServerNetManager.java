package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;

public final class ServerNetManager {
    public static void init() {
        PacketPayloads.LIST.forEach(ServerNetApi::register);
    }
}
