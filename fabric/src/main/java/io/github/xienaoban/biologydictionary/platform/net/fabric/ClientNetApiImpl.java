package io.github.xienaoban.biologydictionary.platform.net.fabric;

import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.PacketUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class ClientNetApiImpl {

    public static <T extends Packet> void register(Class<T> clazz, Packet.Factory<T> factory) {
        if (PacketUtil.hasClientReceiver(clazz)) {
            CustomPacketPayload.Type<T> type = PacketUtil.getType(clazz);
            ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                ClientNetApi.Context ctx = new ClientNetApi.Context(context.client(), context.player());
                ctx.client().execute(() -> payload.clientReceive(ctx));
            });
        }
    }

    public static void send(Packet payload) {
        ClientPlayNetworking.send(payload);
    }
}
