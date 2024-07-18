package io.github.xienaoban.minecraft.biologydictionary.platform.net;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@Environment(EnvType.CLIENT)
public class ClientNetApi {
    public static <T extends CustomPacketPayload> void registerReceiver(Class<T> clazz, ChannelHandler<T> channelHandler) {
        try {
            @SuppressWarnings("unchecked")
            PacketPayloadMeta<T> meta = (PacketPayloadMeta<T>) clazz.getField("META").get(null);
            ClientPlayNetworking.registerGlobalReceiver(meta.type(), (payload, context) ->
                    channelHandler.receive(payload, context.client(), context.player(), context.responseSender())
            );
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends CustomPacketPayload> void send(T payload) {
        ClientPlayNetworking.send(payload);
    }

    @FunctionalInterface
    public interface ChannelHandler<T extends CustomPacketPayload> {
        void receive(T payload, Minecraft client, LocalPlayer player, PacketSender responseSender);
    }
}
