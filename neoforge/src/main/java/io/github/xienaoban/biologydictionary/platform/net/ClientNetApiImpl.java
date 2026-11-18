package io.github.xienaoban.biologydictionary.platform.net;

import net.neoforged.neoforge.network.PacketDistributor;

public final class ClientNetApiImpl implements ClientNetApi.PlatformBridge {
    @Override
    public void send(Packet payload) {
        // NeoForge 21.1 keeps sendToServer on the common PacketDistributor (no ClientPacketDistributor).
        PacketDistributor.sendToServer(payload);
    }
}
