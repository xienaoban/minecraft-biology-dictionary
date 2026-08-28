package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.client.DiscoveryToast;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecordSerializer;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Server notifies client of another player's discovery, shared to everyone: S -> C.
 * No entity id — the discoverer's swing/highlight make no sense for the recipient.
 */
public record SendSharedDiscoveryIncrementalPacket(String discovererName, EntityType<?> entityType,
                                                   DiscoveryRecord record) implements Packet {
    public static final Packet.Factory<SendSharedDiscoveryIncrementalPacket> FACTORY =
            SendSharedDiscoveryIncrementalPacket::new;

    private SendSharedDiscoveryIncrementalPacket(FriendlyByteBuf buf) {
        this(buf.readUtf(), readEntityType(buf), DiscoveryRecordSerializer.readFromBuf(buf));
    }

    private static EntityType<?> readEntityType(FriendlyByteBuf buf) {
        return EntityUtils.getEntityType(IdentifierUtils.fromBuf(buf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(discovererName);
        buf.writeUtf(EntityUtils.getEntityTypeIdName(entityType));
        DiscoveryRecordSerializer.writeToBuf(buf, record);
    }

    @ClientOnly
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        @ClientOnly final class CO { static void receive(
                SendSharedDiscoveryIncrementalPacket packet, ClientNetApi.Context ctx) {
            ClientWorldSession cws = ClientWorldSession.get();
            if (cws == null) {
                LOGGER.warn("Null ClientWorldSession. Ignored.", new RuntimeException());
                return;
            }

            cws.getDiscoveryCacheManager().incrementalSync(packet.entityType, packet.record);
            Minecraft client = ctx.client();
            client.gui.toastManager().addToast(new DiscoveryToast(packet.entityType,
                    TextUtils.translate(Lang.TEXT_SHARED_ENTITY_DISCOVERED, packet.discovererName)));
        }}
        CO.receive(this, ctx);
    }
}
