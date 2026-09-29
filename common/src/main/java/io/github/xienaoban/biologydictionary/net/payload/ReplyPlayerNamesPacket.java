package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Server-to-client reply to {@link RequestPlayerNamesPacket}: display names for the
 * requested player UUIDs. Unknown UUIDs are simply absent; the client keeps showing
 * the UUID.
 */
public record ReplyPlayerNamesPacket(Map<UUID, String> names) implements Packet {
    public static final Packet.Factory<ReplyPlayerNamesPacket> FACTORY = ReplyPlayerNamesPacket::new;

    private ReplyPlayerNamesPacket(FriendlyByteBuf buf) {
        this(readNames(buf));
    }

    private static Map<UUID, String> readNames(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<UUID, String> names = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            names.put(buf.readUUID(), buf.readUtf());
        }
        return names;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(names.size());
        for (Map.Entry<UUID, String> entry : names.entrySet()) {
            buf.writeUUID(entry.getKey());
            buf.writeUtf(entry.getValue());
        }
    }

    @ClientOnly
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        @ClientOnly final class CO { static void receive(ReplyPlayerNamesPacket packet) {
            ClientWorldSession cws = ClientWorldSession.get();
            if (cws == null) {
                LOGGER.warn("Null ClientWorldSession. Ignored.", new RuntimeException());
                return;
            }
            cws.getPlayerNameCache().putAll(packet.names());
        }}
        CO.receive(this);
    }
}
