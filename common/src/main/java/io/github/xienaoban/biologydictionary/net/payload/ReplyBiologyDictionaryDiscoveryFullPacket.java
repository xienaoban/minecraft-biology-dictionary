package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.BiologyDictionaryClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Discovery records sync packet: S -> C.
 * Sent in response to {@link RequestBiologyDictionaryDiscoveryFullPacket}.
 */
public record ReplyBiologyDictionaryDiscoveryFullPacket(Map<EntityType<?>, DiscoveryRecord> discoveries)
        implements Packet {
    public static final Packet.Factory<ReplyBiologyDictionaryDiscoveryFullPacket> FACTORY =
            ReplyBiologyDictionaryDiscoveryFullPacket::new;

    private ReplyBiologyDictionaryDiscoveryFullPacket(FriendlyByteBuf buf) {
        this(readDiscoveries(buf));
    }

    private static Map<EntityType<?>, DiscoveryRecord> readDiscoveries(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<EntityType<?>, DiscoveryRecord> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            Identifier id = Identifier.tryParse(buf.readUtf());
            DiscoveryRecord record = DiscoveryRecord.readFromBuf(buf);
            EntityType<?> type = EntityUtils.getEntityType(id);
            if (type != null) {
                map.put(type, record);
            }
        }
        return map;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(discoveries.size());
        for (Map.Entry<EntityType<?>, DiscoveryRecord> entry : discoveries.entrySet()) {
            buf.writeUtf(EntityUtils.getEntityTypeIdName(entry.getKey()));
            entry.getValue().writeToBuf(buf);
        }
    }

    @ClientOnly
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        @ClientOnly final class CO { static void receive(ReplyBiologyDictionaryDiscoveryFullPacket packet) {
            ClientWorldSession cws = ClientWorldSession.get();
            if (cws == null) {
                LOGGER.warn("Null ClientWorldSession. Ignored.", new RuntimeException());
                return;
            }

            if (cws.getDiscoveryCacheManager().getDelegate() instanceof BiologyDictionaryClientDiscoveryCache cache) {
                cache.onFullSync(packet.discoveries());
                LOGGER.info("Full discovery records received.");
            } else {
                LOGGER.warn("Received wrong discovery strategy. Ignored.", new RuntimeException());
            }
        }}
        CO.receive(this);
    }
}
