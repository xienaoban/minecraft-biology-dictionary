package io.github.xienaoban.minecraft.biologydictionary.platform.net;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@Environment(EnvType.CLIENT)
public final class ClientNetApi {

    public static <T extends CustomPacketPayload> void register(Class<T> clazz) {
        try {
            @SuppressWarnings("unchecked")
            PacketPayloadMeta<T> meta = (PacketPayloadMeta<T>) clazz.getField("META").get(null);
            CustomPacketPayload.Type<T> type = meta.type();
            PacketPayloadMeta.ClientReceiver<T> clientReceiver = meta.clientReceiver();

            if (clientReceiver != null) {
                ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                        clientReceiver.receive(payload, new Context(context.client(), context.player(), context.responseSender()))
                );
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends CustomPacketPayload> void send(T payload) {
        ClientPlayNetworking.send(payload);
    }

    public record Context(Minecraft client, LocalPlayer player, PacketSender responseSender) {}
}
