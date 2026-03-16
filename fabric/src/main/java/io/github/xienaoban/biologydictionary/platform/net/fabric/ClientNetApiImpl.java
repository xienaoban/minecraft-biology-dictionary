package io.github.xienaoban.biologydictionary.platform.net.fabric;

import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.PacketUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public final class ClientNetApiImpl {

    private ClientNetApiImpl() {}

    public static <T extends Packet> void register(Class<T> clazz, Packet.Factory<T> factory) {
        if (PacketUtil.hasClientReceiver(clazz)) {
            ResourceLocation id = PacketUtil.getId(clazz);
            ClientPlayNetworking.registerGlobalReceiver(id, (client, handler, buf, responseSender) -> {
                LocalPlayer player = Objects.requireNonNull(client.player);
                T packet = factory.create(buf);
                client.execute(() -> packet.clientReceive(new ClientNetApi.Context(client, player)));
            });
        }
    }

    public static void send(Packet packet) {
        ResourceLocation id = PacketUtil.getId(packet.getClass());
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.write(buf);
        ClientPlayNetworking.send(id, buf);
    }
}
