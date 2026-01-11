package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import net.minecraft.network.FriendlyByteBuf;

public record RequestServerConfigsPacket() implements Packet {
    public static final Packet.Factory<RequestServerConfigsPacket> FACTORY = buf -> new RequestServerConfigsPacket();

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        String serverConfigsYaml = ConfigsManager.serializeConfigCategory(
            ConfigsManager.getInstance().getServer());
        ServerNetManager.replyServerConfigs(ctx.player(), serverConfigsYaml);
    }
}
