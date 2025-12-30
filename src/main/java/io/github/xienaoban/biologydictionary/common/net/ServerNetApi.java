package io.github.xienaoban.biologydictionary.common.net;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class ServerNetApi {

    public static <T extends Packet> void register(Class<T> clazz, Packet.Factory<T> factory) {
        PacketUtil.registerType(clazz);

        CustomPacketPayload.Type<T> type = PacketUtil.getType(clazz);
        StreamCodec<FriendlyByteBuf, T> codec = PacketUtil.generateCodec(factory);

        // Always register s2c as you are not able to see "hasClientReceiver" on the server.
        PayloadTypeRegistry.playS2C().register(type, codec);

        if (PacketUtil.hasServerReceiver(clazz)) {
            PayloadTypeRegistry.playC2S().register(type, codec);
            ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                Context ctx = new Context(context.server(), context.player(), context.responseSender());
                payload.serverReceive(ctx);
            });
        }
    }

    public static void send(ServerPlayer player, Packet payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public record Context(MinecraftServer server, ServerPlayer player, PacketSender responseSender) {}
}
