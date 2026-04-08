package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.BiologyDictionaryClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Discovery records sync packet: S -> C.
 * Sent in response to {@link RequestBiologyDictionaryDiscoveryFullPacket}.
 */
public record ReplyBiologyDictionaryDiscoveryFullPacket(Map<Identifier, DiscoveryRecord> discoveries) implements Packet {
    public static final Packet.Factory<ReplyBiologyDictionaryDiscoveryFullPacket> FACTORY = ReplyBiologyDictionaryDiscoveryFullPacket::new;

    private ReplyBiologyDictionaryDiscoveryFullPacket(FriendlyByteBuf buf) {
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
            buf.writeBoolean(record.discovered());
            buf.writeLong(record.firstDiscoveryTime());
            buf.writeLong(record.firstDiscoveryTick());
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        final class W { static void receive(ReplyBiologyDictionaryDiscoveryFullPacket packet) {
            ClientWorldSession session = ClientWorldSession.get();
            if (session == null) {
                return;
            }
            if (session.getDiscoveryClientCache().getDelegate() instanceof BiologyDictionaryClientDiscoveryCache cache) {
                cache.onFullSync(packet.discoveries());
                LOGGER.info("Full discovery records received.");
            } else {
                LOGGER.warn("Received wrong discovery strategy. Ignored.");
            }
        }}
        W.receive(this);
    }
}
