package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.client.ClientWorldSession;
import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryClientCache;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Full biology dictionary data sync packet: S -> C.
 * Combines server configs and discovery records in one packet.
 * Sent in response to {@link RequestFullSyncPacket}.
 */
public record ReplyFullSyncPacket(String serverConfigsYaml, Map<Identifier, DiscoveryRecord> discoveries) implements Packet {
    public static final Packet.Factory<ReplyFullSyncPacket> FACTORY = ReplyFullSyncPacket::new;

    private ReplyFullSyncPacket(FriendlyByteBuf buf) {
        this(buf.readUtf(), readDiscoveries(buf));
    }

    private static Map<Identifier, DiscoveryRecord> readDiscoveries(FriendlyByteBuf buf) {
        int hasDiscoveries = buf.readVarInt();
        if (hasDiscoveries == 0) {
            return null;
        }
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
        buf.writeUtf(serverConfigsYaml);
        if (discoveries == null) {
            buf.writeVarInt(0);
        } else {
            buf.writeVarInt(1);
            buf.writeVarInt(discoveries.size());
            for (Map.Entry<Identifier, DiscoveryRecord> entry : discoveries.entrySet()) {
                buf.writeUtf(entry.getKey().toString());
                DiscoveryRecord record = entry.getValue();
                buf.writeBoolean(record.isDiscovered());
                buf.writeLong(record.getFirstDiscoveryTime());
                buf.writeLong(record.getFirstDiscoveryTick());
            }
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        final class W { static void receive(ReplyFullSyncPacket packet, ClientNetApi.Context ctx) {
            Configs.ServerConfigs remoteConfigs = new Configs.ServerConfigs();
            boolean success = ConfigsManager.deserializeConfigCategory(packet.serverConfigsYaml(), remoteConfigs);
            if (!success) {
                return;
            }
            ConfigsManager.setRemoteServerConfigs(remoteConfigs);

            ClientWorldSession clientSession = ClientWorldSession.get();
            if (clientSession == null) {
                return;
            }
            clientSession.getDiscoveryClientCache().updateStrategy(remoteConfigs.getDiscoveryStrategy());
            if (packet.discoveries() != null) {
                clientSession.getDiscoveryClientCache().onFullSync(packet.discoveries());
            }
        }}
        W.receive(this, ctx);
    }
}
