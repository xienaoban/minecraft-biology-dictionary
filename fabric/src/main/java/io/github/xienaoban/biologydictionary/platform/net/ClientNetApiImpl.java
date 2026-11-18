package io.github.xienaoban.biologydictionary.platform.net;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class ClientNetApiImpl implements ClientNetApi.PlatformBridge {
    @Override
    public void send(Packet payload) {
        ResourceLocation id = PacketUtil.getId(payload.getClass());
        FriendlyByteBuf buf = PacketByteBufs.create();
        payload.write(buf);
        ClientPlayNetworking.send(id, buf);
    }
}
