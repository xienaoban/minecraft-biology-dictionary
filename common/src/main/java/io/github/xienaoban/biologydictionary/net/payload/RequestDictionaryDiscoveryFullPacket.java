package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/**
 * Request discovery records from server: C -> S.
 * Only sent when the server's discovery strategy is DICTIONARY.
 */
public record RequestDictionaryDiscoveryFullPacket() implements Packet {
    public static final Packet.Factory<RequestDictionaryDiscoveryFullPacket> FACTORY = RequestDictionaryDiscoveryFullPacket::new;

    private RequestDictionaryDiscoveryFullPacket(FriendlyByteBuf buf) {
        this();
    }

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        ServerPlayer player = ctx.player();
        ServerWorldSession session = ServerWorldSession.get();
        if (session == null) {
            return;
        }
        // TODO: Dictionary only
        Map<Identifier, DiscoveryRecord> discoveries = session.getDiscoveryManager().getDiscoveryRecords(player);
        ServerNetManager.replyDictionaryDiscoveryRecords(player, discoveries);
    }
}
