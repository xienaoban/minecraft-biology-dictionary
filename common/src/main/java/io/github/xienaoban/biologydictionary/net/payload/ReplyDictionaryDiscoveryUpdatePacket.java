package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryClientCache;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

/**
 * Incremental update packet: S -> C.
 * Sent when a single entity is newly discovered.
 */
public record ReplyDictionaryDiscoveryUpdatePacket(Identifier entityTypeId, DiscoveryRecord record) implements Packet {
    public static final Packet.Factory<ReplyDictionaryDiscoveryUpdatePacket> FACTORY = ReplyDictionaryDiscoveryUpdatePacket::new;

    private ReplyDictionaryDiscoveryUpdatePacket(FriendlyByteBuf buf) {
        this(Identifier.tryParse(buf.readUtf()), readRecord(buf));
    }

    private static DiscoveryRecord readRecord(FriendlyByteBuf buf) {
        boolean discovered = buf.readBoolean();
        long time = buf.readLong();
        long tick = buf.readLong();
        return new DiscoveryRecord(discovered, time, tick);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(entityTypeId.toString());
        buf.writeBoolean(record.isDiscovered());
        buf.writeLong(record.getFirstDiscoveryTime());
        buf.writeLong(record.getFirstDiscoveryTick());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        final class W { static void receive(ReplyDictionaryDiscoveryUpdatePacket packet, ClientNetApi.Context ctx) {
            ClientWorldSession clientSession = ClientWorldSession.get();
            if (clientSession == null) { return; }
            DiscoveryClientCache cache = clientSession.getDiscoveryClientCache();
            if (cache != null) {
                cache.onIncrementalSync(packet.entityTypeId(), packet.record());
            }
        }}
        W.receive(this, ctx);
    }
}
