package io.github.xienaoban.biologydictionary.platform.net;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class ServerNetApiImpl implements ServerNetApi.PlatformBridge {
    @Override
    public void send(ServerPlayer player, Packet payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
