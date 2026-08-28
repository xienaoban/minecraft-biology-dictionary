package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.util.ServerUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Request display names for a small set of player UUIDs: C -> S.
 * The client sends this only for names it cannot resolve locally; the server replies
 * with {@link ReplyPlayerNamesPacket}, omitting unknown UUIDs.
 */
public record RequestPlayerNamesPacket(Set<UUID> playerIds) implements Packet {
    private static final int MAX_NAMES = 64;
    public static final Packet.Factory<RequestPlayerNamesPacket> FACTORY = RequestPlayerNamesPacket::new;

    private RequestPlayerNamesPacket(FriendlyByteBuf buf) {
        this(readIds(buf));
    }

    private static Set<UUID> readIds(FriendlyByteBuf buf) {
        int size = Math.max(0, buf.readVarInt());
        Set<UUID> ids = new HashSet<>(Math.min(size, MAX_NAMES));
        for (int i = 0; i < size; i++) {
            UUID id = buf.readUUID();
            if (ids.size() < MAX_NAMES) {
                ids.add(id);
            }
        }
        return ids;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(playerIds.size());
        for (UUID id : playerIds) {
            buf.writeUUID(id);
        }
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        MinecraftServer server = ctx.server();
        Map<UUID, String> names = new HashMap<>();
        for (UUID id : playerIds) {
            ServerUtils.getPlayerName(server, id).ifPresent(name -> names.put(id, name));
        }
        ServerNetManager.replyPlayerNames(ctx.player(), names);
    }
}
