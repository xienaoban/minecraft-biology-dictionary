package io.github.xienaoban.minecraft.biologydictionary.common.net;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.io.PrintWriter;
import java.io.StringWriter;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;

@Environment(EnvType.CLIENT)
public final class ClientNetApi {

    public static <T extends PacketPayload> void register(Class<T> clazz) {
        try {
            @SuppressWarnings("unchecked")
            PacketPayloadMeta<T> meta = (PacketPayloadMeta<T>) clazz.getField("META").get(null);
            CustomPacketPayload.Type<T> type = meta.type();
            PacketPayloadMeta.ClientReceiver<T> clientReceiver = meta.clientReceiver();

            if (clientReceiver != null) {
                ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                    try {
                        clientReceiver.receive(payload, new Context(context.client(), context.player(), context.responseSender()));
                    } catch (Throwable e) {
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        e.printStackTrace(pw);
                        LOGGER.error(sw.toString());
                    }
                });
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends PacketPayload> void send(T payload) {
        ClientPlayNetworking.send(payload);
    }

    public record Context(Minecraft client, LocalPlayer player, PacketSender responseSender) {}
}
