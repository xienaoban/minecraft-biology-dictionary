package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.net.payloads.SendScreenMessagePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ServerNetManager {
    public static void init() {
        PacketPayloads.LIST.forEach(ServerNetApi::register);
    }

    public static void sendScreenMessage(ServerPlayer player, Component message) {
        sendScreenMessage(player, message, Colors.SCREEN_MESSAGE_DEFAULT_COLOR);
    }

    public static void sendScreenMessage(ServerPlayer player, Component message, int color) {
        ServerNetApi.send(player, new SendScreenMessagePacket(message, color));
    }
}
