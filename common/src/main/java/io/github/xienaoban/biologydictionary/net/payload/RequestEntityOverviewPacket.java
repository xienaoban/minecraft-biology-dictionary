package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.ServerEntityOverviewManager;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
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
        EntityType<?> entityType = EntityUtils.getEntityType(entityTypeId);
        if (entityType == null) {
            LOGGER.error("Unknown entity type: {}", entityTypeId);
            BiologyDictionary.sendCenteredWarning(ctx.player(),
                    TextUtils.translate(Lang.TEXT_UNKNOWN_ENTITY_TYPE));
            return;
        }
        if (!ServerEntityOverviewManager.send(ctx.player(), entityType, false)) {
            BiologyDictionary.sendCenteredWarning(ctx.player(),
                    TextUtils.translate(Lang.TEXT_ENTITY_NOT_DISCOVERED));
        }
    }
}
