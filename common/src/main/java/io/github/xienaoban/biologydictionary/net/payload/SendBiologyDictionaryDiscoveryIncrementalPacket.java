package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
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
 * Server notifies client of a new discovery: S -> C.
 * // TODO: implement client-side handling
 */
public record SendBiologyDictionaryDiscoveryIncrementalPacket(EntityType<?> entityType, DiscoveryRecord record) implements Packet {
    public static final Packet.Factory<SendBiologyDictionaryDiscoveryIncrementalPacket> FACTORY = SendBiologyDictionaryDiscoveryIncrementalPacket::new;

    private SendBiologyDictionaryDiscoveryIncrementalPacket(FriendlyByteBuf buf) {
        this(readEntityType(buf), DiscoveryRecord.readFromBuf(buf));
    }

    private static EntityType<?> readEntityType(FriendlyByteBuf buf) {
        Identifier id = Identifier.tryParse(buf.readUtf());
        return EntityUtils.getEntityType(id);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(EntityUtils.getEntityTypeIdName(entityType));
        record.writeToBuf(buf);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        // TODO: proper client-side discovery handling
        ClientWorldSession session = ClientWorldSession.get();
        if (session != null) {
            session.getDiscoveryClientCache().onIncrementalSync(entityType, record);
        }
        BiologyDictionaryClient.sendCenteredInfo(EntityUtils.getEntityTypeNameText(entityType).copy());
    }
}
