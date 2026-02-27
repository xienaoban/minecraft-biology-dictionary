package io.github.xienaoban.biologydictionary.common.net.neoforge;

import dev.architectury.networking.NetworkManager;
import io.github.xienaoban.biologydictionary.common.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientNetApiImpl {

    public static <T extends Packet> void register(Class<T> clazz, Packet.Factory<T> factory) {
        PacketUtil.registerType(clazz);
    }

    public static void send(Packet payload) {
        NetworkManager.sendToServer(payload);
    }
}
