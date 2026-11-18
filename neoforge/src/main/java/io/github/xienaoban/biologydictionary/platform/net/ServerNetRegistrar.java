package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.net.PacketPayloads;
import io.github.xienaoban.biologydictionary.platform.ClientAndServer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
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
            registrar.playBidirectional(type, codec, createHandler(hasClientReceiver));
            return;
        }

        if (hasServerReceiver) {
            registrar.playToServer(type, codec, createHandler(false));
            return;
        }

        if (hasClientReceiver) {
            registrar.playToClient(type, codec, createHandler(true));
        }
    }

    /**
     * NeoForge 21.1 takes the receiving handler at registration time for every direction, and has no
     * client-only registration event. A handler only ever runs on the side that receives the payload,
     * so the physical distribution is enough to pick it.
     */
    @ClientAndServer
    private static <T extends Packet> IPayloadHandler<T> createHandler(boolean clientReceives) {
        if (clientReceives) {
            if (FMLEnvironment.dist.isClient()) {
                return ClientNetRegistrar.createClientHandler();
            }
            // A dedicated server never receives a client-bound payload.
            return (payload, context) -> {};
        }
        return (payload, context) -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ServerNetApi.Context ctx = new ServerNetApi.Context(player.level().getServer(), player);
            payload.serverReceive(ctx);
        };
    }
}
