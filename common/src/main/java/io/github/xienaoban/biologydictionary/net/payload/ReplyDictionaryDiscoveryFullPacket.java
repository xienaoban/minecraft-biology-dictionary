package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryClientCache;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Discovery records sync packet: S -> C.
 * Sent in response to {@link RequestDictionaryDiscoveryFullPacket}.
 */
public record ReplyDictionaryDiscoveryFullPacket(Map<Identifier, DiscoveryRecord> discoveries) implements Packet {
    public static final Packet.Factory<ReplyDictionaryDiscoveryFullPacket> FACTORY = ReplyDictionaryDiscoveryFullPacket::new;

    private ReplyDictionaryDiscoveryFullPacket(FriendlyByteBuf buf) {
        this(readDiscoveries(buf));
    }

    private static Map<Identifier, DiscoveryRecord> readDiscoveries(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<Identifier, DiscoveryRecord> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            Identifier id = Identifier.tryParse(buf.readUtf());
            boolean discovered = buf.readBoolean();
            long time = buf.readLong();
            long tick = buf.readLong();
            if (discovered) {
                map.put(id, new DiscoveryRecord(true, time, tick));
            }
        }
        return map;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(discoveries.size());
        for (Map.Entry<Identifier, DiscoveryRecord> entry : discoveries.entrySet()) {
            buf.writeUtf(entry.getKey().toString());
            DiscoveryRecord record = entry.getValue();
            buf.writeBoolean(record.isDiscovered());
            buf.writeLong(record.getFirstDiscoveryTime());
            buf.writeLong(record.getFirstDiscoveryTick());
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        final class W { static void receive(ReplyDictionaryDiscoveryFullPacket packet) {
            ClientWorldSession clientSession = ClientWorldSession.get();
            if (clientSession == null) {
                return;
            }
            DiscoveryClientCache cache = clientSession.getDiscoveryClientCache();
            // TODO: not interface anymore, do it if instanceof DictionaryClientCache
            cache.onFullSync(packet.discoveries());
        }}
        W.receive(this);
    }
}
