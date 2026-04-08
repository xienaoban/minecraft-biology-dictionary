package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.BiologyDictionaryClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

/**
 * Incremental update packet: S -> C.
 * Sent when a single entity is newly discovered.
 */
public record ReplyBiologyDictionaryDiscoveryIncrementalPacket(EntityType<?> entityType, DiscoveryRecord record) implements Packet {
    public static final Packet.Factory<ReplyBiologyDictionaryDiscoveryIncrementalPacket> FACTORY = ReplyBiologyDictionaryDiscoveryIncrementalPacket::new;

    private ReplyBiologyDictionaryDiscoveryIncrementalPacket(FriendlyByteBuf buf) {
        this(readEntityType(buf), readRecord(buf));
    }

    private static EntityType<?> readEntityType(FriendlyByteBuf buf) {
        Identifier id = Identifier.tryParse(buf.readUtf());
        return EntityUtils.getEntityType(id);
    }

    private static DiscoveryRecord readRecord(FriendlyByteBuf buf) {
        boolean discovered = buf.readBoolean();
        long time = buf.readLong();
        long tick = buf.readLong();
        return new DiscoveryRecord(discovered, time, tick);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(EntityUtils.getEntityTypeIdName(entityType));
        buf.writeBoolean(record.discovered());
        buf.writeLong(record.firstDiscoveryTime());
        buf.writeLong(record.firstDiscoveryTick());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        final class W { static void receive(ReplyBiologyDictionaryDiscoveryIncrementalPacket packet, ClientNetApi.Context ctx) {
            ClientWorldSession session = ClientWorldSession.get();
            if (session == null) { return; }
            if (packet.entityType() != null
                    && session.getDiscoveryClientCache().getDelegate() instanceof BiologyDictionaryClientDiscoveryCache cache) {
                cache.onIncrementalSync(packet.entityType(), packet.record());
            }
        }}
        W.receive(this, ctx);
    }
}
