package io.github.xienaoban.biologydictionary.platform.net.fabric;

import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.PacketUtil;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ServerNetApiImpl {

    private ServerNetApiImpl() {}

    public static <T extends Packet> void register(Class<T> clazz, Packet.Factory<T> factory) {
        PacketUtil.registerId(clazz);

        ResourceLocation id = PacketUtil.getId(clazz);

        if (PacketUtil.hasServerReceiver(clazz)) {
            ServerPlayNetworking.registerGlobalReceiver(id, (server, player, listener, buf, responseSender) -> {
                T packet = factory.create(buf);
                server.execute(() -> packet.serverReceive(new ServerNetApi.Context(server, player)));
            });
        }
    }

    public static void send(ServerPlayer player, Packet packet) {
        ResourceLocation id = PacketUtil.getId(packet.getClass());
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.write(buf);
        ServerPlayNetworking.send(player, id, buf);
    }
}
