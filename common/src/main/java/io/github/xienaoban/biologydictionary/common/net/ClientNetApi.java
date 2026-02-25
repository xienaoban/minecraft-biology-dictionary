package io.github.xienaoban.biologydictionary.common.net;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@Environment(EnvType.CLIENT)
public final class ClientNetApi {

    public static <T extends Packet> void register(Class<T> clazz, Packet.Factory<T> factory) {
        if (PacketUtil.hasClientReceiver(clazz)) {
            CustomPacketPayload.Type<T> type = PacketUtil.getType(clazz);
            ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                Context ctx = new Context(context.client(), context.player(), context.responseSender());
                payload.clientReceive(ctx);
            });
        }
    }

    public static void send(Packet payload) {
        ClientPlayNetworking.send(payload);
    }

    public record Context(Minecraft client, LocalPlayer player, PacketSender responseSender) {}
}
