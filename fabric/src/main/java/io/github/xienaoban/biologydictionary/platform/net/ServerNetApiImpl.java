package io.github.xienaoban.biologydictionary.platform.net;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ServerNetApiImpl implements ServerNetApi.PlatformBridge {
    @Override
    public void send(ServerPlayer player, Packet payload) {
        ResourceLocation id = PacketUtil.getId(payload.getClass());
        FriendlyByteBuf buf = PacketByteBufs.create();
        payload.write(buf);
        ServerPlayNetworking.send(player, id, buf);
    }

    @Override
    public boolean canSend(ServerPlayer player, Class<? extends Packet> packetClass) {
        return ServerPlayNetworking.canSend(player, PacketUtil.getId(packetClass));
    }
}
