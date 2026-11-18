package io.github.xienaoban.biologydictionary.platform.net;

import net.minecraft.server.level.ServerPlayer;

public final class ServerNetApiImpl implements ServerNetApi.PlatformBridge {
    @Override
    public void send(ServerPlayer player, Packet payload) {
        ServerNetRegistrar.sendToPlayer(player, payload);
    }

    @Override
    public boolean canSend(ServerPlayer player, Class<? extends Packet> packetClass) {
        return ServerNetRegistrar.canSend(player);
    }
}
