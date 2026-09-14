package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.net.PacketPayloads;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@Environment(EnvType.CLIENT)
public final class ClientNetRegistrar {
    private ClientNetRegistrar() {}

    public static void registerClientReceivers() {
        for (PacketPayloads.Entry<?> payload : PacketPayloads.ENTRIES) {
            registerClientReceiver(payload);
        }
    }

    private static <T extends Packet> void registerClientReceiver(PacketPayloads.Entry<T> payload) {
        registerClientReceiver(payload.packetClass());
    }

    private static <T extends Packet> void registerClientReceiver(Class<T> clazz) {
        PacketUtil.registerType(clazz);

        if (PacketUtil.hasClientReceiver(clazz)) {
            CustomPacketPayload.Type<T> type = PacketUtil.getType(clazz);
            ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                ClientNetApi.Context ctx = new ClientNetApi.Context(context.client(), context.player());
                context.client().execute(() -> payload.clientReceive(ctx));
            });
        }
    }
}
