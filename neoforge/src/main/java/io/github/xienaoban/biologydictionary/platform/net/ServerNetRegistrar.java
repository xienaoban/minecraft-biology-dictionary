package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.net.PacketPayloads;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ServerNetRegistrar {
    private ServerNetRegistrar() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        for (PacketPayloads.Entry<?> payload : PacketPayloads.ENTRIES) {
            registerPayload(event, payload);
        }
    }

    private static <T extends Packet> void registerPayload(RegisterPayloadHandlersEvent event,
                                                           PacketPayloads.Entry<T> payload) {
        registerPayload(event, payload.packetClass(), payload.factory());
    }

    private static <T extends Packet> void registerPayload(RegisterPayloadHandlersEvent event, Class<T> clazz,
                                                           Packet.Factory<T> factory) {
        PacketUtil.registerType(clazz);

        CustomPacketPayload.Type<T> type = PacketUtil.getType(clazz);
        StreamCodec<RegistryFriendlyByteBuf, T> codec = PacketUtil.generatePlayCodec(factory);
        // Optional: a peer without the mod must still be able to connect. The namespace doubles as
        // the version string, which is what the architectury builds negotiated.
        PayloadRegistrar registrar = event.registrar(type.id().getNamespace()).optional();
        boolean hasServerReceiver = PacketUtil.hasServerReceiver(clazz);
        boolean hasClientReceiver = PacketUtil.hasClientReceiver(clazz);

        if (hasServerReceiver && hasClientReceiver) {
            registrar.playBidirectional(type, codec, (payload, context) -> {
                ServerPlayer player = (ServerPlayer) context.player();
                ServerNetApi.Context ctx = new ServerNetApi.Context(player.level().getServer(), player);
                payload.serverReceive(ctx);
            });
            return;
        }

        if (hasServerReceiver) {
            registrar.playToServer(type, codec, (payload, context) -> {
                ServerPlayer player = (ServerPlayer) context.player();
                ServerNetApi.Context ctx = new ServerNetApi.Context(player.level().getServer(), player);
                payload.serverReceive(ctx);
            });
            return;
        }

        if (hasClientReceiver) {
            registrar.playToClient(type, codec);
        }
    }
}
