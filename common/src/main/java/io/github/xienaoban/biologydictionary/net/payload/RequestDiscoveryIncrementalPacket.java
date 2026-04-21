package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Client requests to register a discovery: C -> S.
 * The caller should optimistically insert into the local cache before sending.
 */
public record RequestDiscoveryIncrementalPacket(int entityId, DiscoverySource source) implements Packet {
    public static final Packet.Factory<RequestDiscoveryIncrementalPacket> FACTORY = RequestDiscoveryIncrementalPacket::new;

    private RequestDiscoveryIncrementalPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), DiscoverySource.valueOf(buf.readUtf()));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeUtf(source.name());
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        ServerPlayer player = ctx.player();
        ServerWorldSession session = ServerWorldSession.get();
        if (session == null) {
            return;
        }
        Entity entity = player.level().getEntity(entityId);
        if (entity == null) {
            return;
        }
        source.dispatch(session.getDiscoveryManager().getStrategy(), player, entity);
    }
}
