package io.github.xienaoban.biologydictionary.platform.net.neoforge;

import dev.architectury.networking.NetworkManager;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public final class ClientNetApiImpl {

    public static <T extends Packet> void register(Class<T> clazz, Packet.Factory<T> factory) {
        if (PacketUtil.hasClientReceiver(clazz)) {
            CustomPacketPayload.Type<T> type = PacketUtil.getType(clazz);
            StreamCodec<FriendlyByteBuf, T> codec = PacketUtil.generateCodec(factory);
            NetworkManager.registerReceiver(NetworkManager.Side.S2C, type, codec, (payload, context) -> {
                Minecraft client = Minecraft.getInstance();
                ClientNetApi.Context ctx = new ClientNetApi.Context(client, ClientUtils.getClientPlayer(client));
                ctx.client().execute(() -> payload.clientReceive(ctx));
            });
        }
    }

    public static void send(Packet payload) {
        NetworkManager.sendToServer(payload);
    }
}
