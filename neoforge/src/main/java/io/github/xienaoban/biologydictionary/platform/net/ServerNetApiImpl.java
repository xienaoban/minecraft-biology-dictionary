package io.github.xienaoban.biologydictionary.platform.net;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

public final class ServerNetApiImpl implements ServerNetApi.PlatformBridge {
    @Override
    public void send(ServerPlayer player, Packet payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public boolean canSend(ServerPlayer player, Class<? extends Packet> packetClass) {
        return NetworkRegistry.hasChannel(player.connection, PacketUtil.getType(packetClass).id());
    }
}
