package io.github.xienaoban.biologydictionary.common.net.neoforge;

import dev.architectury.networking.NetworkManager;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketUtil;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import net.minecraft.server.level.ServerPlayer;

public final class ServerNetApiImpl {

    public static <T extends Packet> void register(Class<T> clazz, Packet.Factory<T> factory) {
        PacketUtil.registerType(clazz);
        // todo
    }

    public static void send(ServerPlayer player, Packet payload) {
        NetworkManager.sendToPlayer(player, payload);
    }
}
