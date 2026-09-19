package io.github.xienaoban.biologydictionary.platform.net.forge;

import dev.architectury.networking.NetworkManager;
import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.PacketUtil;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ConnectionData;
import net.minecraftforge.network.NetworkHooks;

@SuppressWarnings("unused")
public final class ServerNetApiImpl {

    public static <T extends Packet> void register(Class<T> clazz, Packet.Factory<T> factory) {
        PacketUtil.registerId(clazz);

        if (PacketUtil.hasServerReceiver(clazz)) {
            ResourceLocation id = PacketUtil.getId(clazz);
            NetworkManager.registerReceiver(NetworkManager.Side.C2S, id, (buf, context) -> {
                T packet = factory.create(buf);
                ServerPlayer player = (ServerPlayer) context.getPlayer();
                MinecraftServer server = PlayerUtils.getServer(player);
                ServerNetApi.Context ctx = new ServerNetApi.Context(server, player);
                server.execute(() -> packet.serverReceive(ctx));
            });
        }
    }

    public static void send(ServerPlayer player, Packet packet) {
        ResourceLocation id = PacketUtil.getId(packet.getClass());
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        NetworkManager.sendToPlayer(player, id, buf);
    }

    public static boolean canSend(ServerPlayer player, Class<? extends Packet> packetClass) {
        ConnectionData connectionData = NetworkHooks.getConnectionData(player.connection.connection);
        return connectionData == null || connectionData.getModList().contains(BiologyDictionary.MOD_ID);
    }
}
