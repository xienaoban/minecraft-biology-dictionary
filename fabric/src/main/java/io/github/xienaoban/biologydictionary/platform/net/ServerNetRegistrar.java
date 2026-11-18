package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.net.PacketPayloads;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public final class ServerNetRegistrar {
    private ServerNetRegistrar() {}

    public static void registerCommonPayloads() {
        for (PacketPayloads.Entry<?> payload : PacketPayloads.ENTRIES) {
            registerCommonPayload(payload);
        }
    }

    private static <T extends Packet> void registerCommonPayload(PacketPayloads.Entry<T> payload) {
        registerCommonPayload(payload.packetClass(), payload.factory());
    }

    private static <T extends Packet> void registerCommonPayload(Class<T> clazz, Packet.Factory<T> factory) {
        // Ids are registered for every packet on both sides, so sending never depends on the receiver.
        PacketUtil.registerId(clazz);

        if (PacketUtil.hasServerReceiver(clazz)) {
            ResourceLocation id = PacketUtil.getId(clazz);
            ServerPlayNetworking.registerGlobalReceiver(id, (server, player, handler, buf, responseSender) -> {
                T packet = factory.create(buf);
                ServerNetApi.Context ctx = new ServerNetApi.Context(server, player);
                server.execute(() -> packet.serverReceive(ctx));
            });
        }
    }
}
