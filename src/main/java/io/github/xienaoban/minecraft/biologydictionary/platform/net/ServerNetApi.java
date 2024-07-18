package io.github.xienaoban.minecraft.biologydictionary.platform.net;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ServerNetApi {
    public static <T extends CustomPacketPayload> void registerReceiver(Class<T> clazz, ChannelHandler<T> channelHandler) {
        try {
            @SuppressWarnings("unchecked")
            PacketPayloadMeta<T> meta = (PacketPayloadMeta<T>) clazz.getField("META").get(null);
            ServerPlayNetworking.registerGlobalReceiver(meta.type(), (payload, context) ->
                    channelHandler.receive(payload, context.server(), context.player(), context.responseSender())
            );
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends CustomPacketPayload> void send(ServerPlayer player, T payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @FunctionalInterface
    public interface ChannelHandler<T extends CustomPacketPayload> {
        void receive(T payload, MinecraftServer server, ServerPlayer player, PacketSender responseSender);
    }
}
