package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * Request server configs from server: C -> S.
 * Sent on world connect to get server configs.
 */
public record RequestServerConfigsPacket() implements Packet {
    public static final Packet.Factory<RequestServerConfigsPacket> FACTORY = RequestServerConfigsPacket::new;

    private RequestServerConfigsPacket(FriendlyByteBuf buf) {
        this();
    }

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        ServerPlayer player = ctx.player();
        String serverConfigsYaml = ConfigsManager.serializeConfigCategory(ConfigsManager.getServer());
        ServerNetManager.replyServerConfigs(player, serverConfigsYaml);
    }
}
