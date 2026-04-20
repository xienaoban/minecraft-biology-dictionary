package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryManager;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.strategy.BiologyDictionaryDiscoveryStrategy;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

/**
 * Client reports a discovery event to server: C -> S.
 * Sent when the player opens the entity detail screen.
 * The record (including timestamps) is generated on the client side.
 */
public record SendBiologyDictionaryDiscoveryIncrementalPacket(EntityType<?> entityType, DiscoveryRecord record) implements Packet {
    public static final Packet.Factory<SendBiologyDictionaryDiscoveryIncrementalPacket> FACTORY = SendBiologyDictionaryDiscoveryIncrementalPacket::new;

    private SendBiologyDictionaryDiscoveryIncrementalPacket(FriendlyByteBuf buf) {
        this(readEntityType(buf), readRecord(buf));
    }

    private static EntityType<?> readEntityType(FriendlyByteBuf buf) {
        Identifier id = Identifier.tryParse(buf.readUtf());
        return EntityUtils.getEntityType(id);
    }

    private static DiscoveryRecord readRecord(FriendlyByteBuf buf) {
        long time = buf.readLong();
        long tick = buf.readLong();
        return new DiscoveryRecord(time, tick);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(EntityUtils.getEntityTypeIdName(entityType));
        buf.writeLong(record.firstDiscoveryTime());
        buf.writeLong(record.firstDiscoveryTick());
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        ServerPlayer player = ctx.player();
        ServerWorldSession session = ServerWorldSession.get();
        if (session == null) {
            return;
        }
        DiscoveryManager manager = session.getDiscoveryManager();
        if (manager.getStrategy() instanceof BiologyDictionaryDiscoveryStrategy strategy) {
            strategy.setDiscovered(player, entityType, record);
        }
    }
}
