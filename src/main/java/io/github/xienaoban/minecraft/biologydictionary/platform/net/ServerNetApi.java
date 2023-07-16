package io.github.xienaoban.minecraft.biologydictionary.platform.net;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class ServerNetApi {
    public static void registerReceiver(ResourceLocation channelName, ChannelHandler channelHandler) {
        ServerPlayNetworking.registerGlobalReceiver(channelName, channelHandler::receive);
    }

    public static void send(ServerPlayer player, ResourceLocation channelName, FriendlyByteBuf buf) {
        ServerPlayNetworking.send(player, channelName, buf);
    }

    public interface ChannelHandler {
        void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender);
    }
}
