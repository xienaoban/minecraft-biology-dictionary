package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/**
 * Request full biology dictionary data from server: C -> S.
 * Sent on world connect to get server configs + discovery data in one round-trip.
 */
public record RequestFullSyncPacket() implements Packet {
    public static final Packet.Factory<RequestFullSyncPacket> FACTORY = RequestFullSyncPacket::new;

    private RequestFullSyncPacket(FriendlyByteBuf buf) {
        this();
    }

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        ServerPlayer player = ctx.player();
        String serverConfigsYaml = ConfigsManager.serializeConfigCategory(ConfigsManager.getServer());

        Map<Identifier, DiscoveryRecord> discoveries = null;
        if (!player.isCreative()) {
            ServerWorldSession session = ServerWorldSession.get();
            discoveries = session.getDiscoveryManager().getDiscoveryRecords(player);
        }

        ServerNetManager.replyFullSync(player, serverConfigsYaml, discoveries);
    }
}
