package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.client.DiscoveryToast;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Server notifies client of a new discovery: S -> C.
 */
public record SendDiscoveryIncrementalPacket(int entityId, EntityType<?> entityType, DiscoveryRecord record) implements Packet {
    public static final Packet.Factory<SendDiscoveryIncrementalPacket> FACTORY = SendDiscoveryIncrementalPacket::new;

    private SendDiscoveryIncrementalPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), readEntityType(buf), DiscoveryRecord.readFromBuf(buf));
    }

    private static EntityType<?> readEntityType(FriendlyByteBuf buf) {
        Identifier id = Identifier.tryParse(buf.readUtf());
        return EntityUtils.getEntityType(id);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeUtf(EntityUtils.getEntityTypeIdName(entityType));
        record.writeToBuf(buf);
    }

    @ClientOnly
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        @ClientOnly final class CO { static void receive(SendDiscoveryIncrementalPacket packet, ClientNetApi.Context ctx) {
            ClientWorldSession cws = ClientWorldSession.get();
            if (cws == null) {
                LOGGER.warn("Null ClientWorldSession. Ignored.", new RuntimeException());
                return;
            }

            Minecraft client = ctx.client();
            LocalPlayer player = ctx.player();
            ClientLevel level = ClientUtils.getClientLevel(client);

            // Update discovery cache
            cws.getDiscoveryClientCache().incrementalSync(packet.entityType, packet.record);

            // Show toast
            client.getToastManager().addToast(new DiscoveryToast(packet.entityType));

            // Swing if INTERACT
            if (packet.record.source() == DiscoverySource.INTERACT) {
                player.swing(InteractionHand.MAIN_HAND);
            }

            // Highlight the discovered entity for 4 seconds
            Entity target = level != null ? level.getEntity(packet.entityId) : null;
            if (target != null) {
                ClientWorldSession.get().getHighlightManager().highlightEntity(target, 4 * 20);
            }
        }}
        CO.receive(this, ctx);
    }
}
