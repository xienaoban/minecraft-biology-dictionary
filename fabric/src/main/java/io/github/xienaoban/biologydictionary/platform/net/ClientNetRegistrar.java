package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.net.PacketPayloads;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class ClientNetRegistrar {
    private ClientNetRegistrar() {}

    public static void registerClientReceivers() {
        for (PacketPayloads.Entry<?> payload : PacketPayloads.ENTRIES) {
            registerClientReceiver(payload);
        }
    }

    private static <T extends Packet> void registerClientReceiver(PacketPayloads.Entry<T> payload) {
        registerClientReceiver(payload.packetClass(), payload.factory());
    }

    private static <T extends Packet> void registerClientReceiver(Class<T> clazz, Packet.Factory<T> factory) {
        PacketUtil.registerId(clazz);

        if (PacketUtil.hasClientReceiver(clazz)) {
            ResourceLocation id = PacketUtil.getId(clazz);
            ClientPlayNetworking.registerGlobalReceiver(id, (client, handler, buf, responseSender) -> {
                T packet = factory.create(buf);
                ClientNetApi.Context ctx = new ClientNetApi.Context(client, ClientUtils.getClientPlayer(client));
                client.execute(() -> packet.clientReceive(ctx));
            });
        }
    }
}
