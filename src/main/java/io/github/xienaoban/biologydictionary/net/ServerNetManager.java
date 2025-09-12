package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.net.payload.SendCenteredMessagePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ServerNetManager {
    public static void init() {
        PacketPayloads.LIST.forEach(ServerNetApi::register);
    }

    public static void sendCenteredMessage(ServerPlayer player, Component message) {
        ServerNetApi.send(player, new SendCenteredMessagePacket(message));
    }
}
