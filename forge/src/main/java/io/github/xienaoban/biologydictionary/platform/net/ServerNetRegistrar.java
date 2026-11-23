package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.net.PacketPayloads;
import io.github.xienaoban.biologydictionary.platform.ClientAndServer;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class ServerNetRegistrar {
    private ServerNetRegistrar() {}

    private static final String PROTOCOL_VERSION = "1";

    // Optional channel: a peer without the mod must still be able to connect, so every reported
    // version is accepted. Payload ids and codecs still have to match for the packets to be read.
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            IdentifierUtils.bd("main"),
            () -> PROTOCOL_VERSION,
            version -> true,
            version -> true);

    private static int nextIndex = 0;

    public static void register() {
        for (PacketPayloads.Entry<?> payload : PacketPayloads.ENTRIES) {
            registerPayload(payload);
        }
    }

    private static <T extends Packet> void registerPayload(PacketPayloads.Entry<T> payload) {
        registerPayload(payload.packetClass(), payload.factory());
    }

    private static <T extends Packet> void registerPayload(Class<T> clazz, Packet.Factory<T> factory) {
        // Ids are registered for every packet on both sides, so sending never depends on the receiver.
        PacketUtil.registerId(clazz);

        if (!PacketUtil.hasServerReceiver(clazz) && !PacketUtil.hasClientReceiver(clazz)) {
            return;
        }

        // Forge 1.20.1 keeps one handler per message class for the whole channel and takes that
        // handler at registration time, so the direction is left open here and resolved from the
        // receiving side in createHandler() below. The index only has to be unique per channel.
        CHANNEL.messageBuilder(clazz, nextIndex++)
                .encoder((packet, buf) -> packet.write(buf))
                .decoder(factory::create)
                .consumerMainThread(createHandler())
                .add();
    }

    /**
     * Forge 1.20.1 has no per-direction consumer and no client-only registration event, so the
     * handler is shared and the arrival direction picks the side. A handler only ever runs on the
     * side that receives the payload, which is also what keeps {@link ClientNetRegistrar} off the
     * dedicated server.
     */
    @ClientAndServer
    private static <T extends Packet> BiConsumer<T, Supplier<NetworkEvent.Context>> createHandler() {
        return (payload, contextSupplier) -> {
            NetworkEvent.Context context = contextSupplier.get();
            if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                ClientNetRegistrar.receive(payload);
                return;
            }
            ServerPlayer player = context.getSender();
            ServerNetApi.Context ctx = new ServerNetApi.Context(PlayerUtils.getServer(player), player);
            payload.serverReceive(ctx);
        };
    }

    static void sendToServer(Packet payload) {
        CHANNEL.sendToServer(payload);
    }

    static void sendToPlayer(ServerPlayer player, Packet payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    static boolean canSend(ServerPlayer player) {
        // A peer that has the mod but not this channel is a pre-rework architectury build, which
        // cannot read our packets. Callers fall back to a vanilla message when this returns false.
        Connection connection = player.connection.connection;
        return connection != null && CHANNEL.isRemotePresent(connection);
    }
}
