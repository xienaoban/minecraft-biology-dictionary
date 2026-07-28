package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.EntityOverviewCache;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Request packet for getting entity type reference properties.
 * Client sends this to server to request default/overview data for an entity type.
 */
public record RequestEntityOverviewPacket(String entityTypeId) implements Packet {
    public static final Packet.Factory<RequestEntityOverviewPacket> FACTORY = RequestEntityOverviewPacket::new;

    private RequestEntityOverviewPacket(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(entityTypeId);
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        WorldSession ws = WorldSession.get();
        if (ws == null) {
            LOGGER.warn("Null WorldSession. Ignored.", new RuntimeException());
            return;
        }
        ServerWorldSession sws = ServerWorldSession.get();
        if (sws == null) {
            LOGGER.warn("Null ServerWorldSession. Ignored.", new RuntimeException());
            return;
        }

        ReplyEntityOverviewPacket toSend;
        EntityType<?> entityType = EntityUtils.getEntityType(entityTypeId);
        if (entityType != null) {
            // Server-side guard: check if entity is locked
            if (!ConfigsManager.getServer().isAllowOverviewForUndiscoveredEntities()
                    && !sws.getDiscoveryManager().isDiscovered(ctx.player(), entityType)) {
                toSend = new ReplyEntityOverviewPacket(false, entityTypeId, null, null);
            } else {
                EntityOverviewCache.CacheEntry cached = ws.getEntityOverviewCache()
                        .getOrCreate(entityType, ctx.player().level());
                toSend = new ReplyEntityOverviewPacket(
                        cached.isValid(), entityTypeId, cached.vanillaNbt(), cached.extraNbt());
            }
        } else {
            LOGGER.error("Unknown entity type: {}", entityTypeId);
            toSend = new ReplyEntityOverviewPacket(false, entityTypeId, null, null);
        }

        ServerNetApi.send(ctx.player(), toSend);
    }
}
