package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.api.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySources;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Client requests to register a discovery: C -> S.
 * The caller validates on the client first, then sends this request; the server is
 * authoritative and replies with the record via {@link SendDiscoveryIncrementalPacket}.
 */
public record RequestDiscoveryIncrementalPacket(int entityId, DiscoverySource source) implements Packet {
    public static final Packet.Factory<RequestDiscoveryIncrementalPacket> FACTORY =
            RequestDiscoveryIncrementalPacket::new;

    private RequestDiscoveryIncrementalPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), DiscoverySources.parseSource(buf.readUtf()));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeUtf(source.id().toString());
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        ServerWorldSession sws = ServerWorldSession.get();
        if (sws == null) {
            LOGGER.warn("Null ServerWorldSession. Ignored.", new RuntimeException());
            return;
        }

        ServerPlayer player = ctx.player();
        Entity entity = player.level().getEntity(entityId);
        if (entity == null) {
            return;
        }
        sws.getDiscoveryManager().onDiscoveryEvent(source, player, entity);
    }
}
