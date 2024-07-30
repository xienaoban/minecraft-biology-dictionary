package io.github.xienaoban.minecraft.biologydictionary.platform.net;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class ServerNetApi {
    public static <T extends CustomPacketPayload> void register(Class<T> clazz) {
        try {
            @SuppressWarnings("unchecked")
            PacketPayloadMeta<T> meta = (PacketPayloadMeta<T>) clazz.getField("META").get(null);
            CustomPacketPayload.Type<T> type = meta.type();
            StreamCodec<FriendlyByteBuf, T> codec = meta.codec();
            PacketPayloadMeta.ServerReceiver<T> serverReceiver = meta.serverReceiver();
            PacketPayloadMeta.ClientReceiver<T> clientReceiver = meta.clientReceiver();

            if (clientReceiver != null) {
                PayloadTypeRegistry.playS2C().register(type, codec);
            }

            if (serverReceiver != null) {
                PayloadTypeRegistry.playC2S().register(type, codec);
                ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                        serverReceiver.receive(payload, new ServerNetApi.Context(context.server(), context.player(), context.responseSender()))
                );
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends CustomPacketPayload> void send(ServerPlayer player, T payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public record Context(MinecraftServer server, ServerPlayer player, PacketSender responseSender) {}
}
