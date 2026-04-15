package io.github.xienaoban.biologydictionary.platform.net.neoforge;

import dev.architectury.networking.NetworkManager;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.PacketUtil;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

@SuppressWarnings("unused")
public final class ServerNetApiImpl {

    public static <T extends Packet> void register(Class<T> clazz, Packet.Factory<T> factory) {
        PacketUtil.registerType(clazz);

        CustomPacketPayload.Type<T> type = PacketUtil.getType(clazz);
        StreamCodec<FriendlyByteBuf, T> codec = PacketUtil.generateCodec(factory);

        // Not like Fabric, do not register it here.
        // NetworkManager.registerS2CPayloadType(type, codec);

        if (PacketUtil.hasServerReceiver(clazz)) {
            NetworkManager.registerReceiver(NetworkManager.Side.C2S, type, codec, (payload, context) -> {
                ServerPlayer player = (ServerPlayer) context.getPlayer();
                ServerNetApi.Context ctx = new ServerNetApi.Context(PlayerUtils.getServer(player), player);
                ctx.server().execute(() -> payload.serverReceive(ctx));
            });
        }
    }

    public static void send(ServerPlayer player, Packet payload) {
        NetworkManager.sendToPlayer(player, payload);
    }
}
