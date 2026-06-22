package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.net.PacketPayloads;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

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
        PacketUtil.registerType(clazz);

        CustomPacketPayload.Type<T> type = PacketUtil.getType(clazz);
        StreamCodec<RegistryFriendlyByteBuf, T> codec = PacketUtil.generatePlayCodec(factory);

        PayloadTypeRegistry.clientboundPlay().register(type, codec);

        if (PacketUtil.hasServerReceiver(clazz)) {
            PayloadTypeRegistry.serverboundPlay().register(type, codec);
            ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                ServerNetApi.Context ctx = new ServerNetApi.Context(context.server(), context.player());
                context.server().execute(() -> payload.serverReceive(ctx));
            });
        }
    }
}
