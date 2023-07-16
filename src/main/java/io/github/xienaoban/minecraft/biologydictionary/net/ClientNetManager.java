package io.github.xienaoban.minecraft.biologydictionary.net;

import io.github.xienaoban.minecraft.biologydictionary.platform.net.ClientNetApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

@Environment(EnvType.CLIENT)
public final class ClientNetManager {
    public static void init() {
    }

    public static void sendRequestBookItem() {
        ClientNetApi.send(Channels.REQUEST_BOOK_ITEM, PacketByteBufs.empty());
    }
}
