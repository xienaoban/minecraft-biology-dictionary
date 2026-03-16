package io.github.xienaoban.biologydictionary.platform.net.forge;

import dev.architectury.networking.NetworkManager;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.PacketUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public final class ClientNetApiImpl {

    public static <T extends Packet> void register(Class<T> clazz, Packet.Factory<T> factory) {
        if (PacketUtil.hasClientReceiver(clazz)) {
            ResourceLocation id = PacketUtil.getId(clazz);
            NetworkManager.registerReceiver(NetworkManager.Side.S2C, id, (buf, context) -> {
                try {
                    T packet = factory.create(buf);
                    Minecraft client = Minecraft.getInstance();
                    LocalPlayer player = ClientUtils.getClientPlayer(client);
                    if (player != context.getPlayer()) {
                        throw new AssertionError();
                    }
                    ClientNetApi.Context ctx = new ClientNetApi.Context(client, player);
                    client.execute(() -> packet.clientReceive(ctx));
                } catch (Exception e) {
                    // Log error
                }
            });
        }
    }

    public static void send(Packet packet) {
        ResourceLocation id = PacketUtil.getId(packet.getClass());
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        NetworkManager.sendToServer(id, buf);
    }
}
