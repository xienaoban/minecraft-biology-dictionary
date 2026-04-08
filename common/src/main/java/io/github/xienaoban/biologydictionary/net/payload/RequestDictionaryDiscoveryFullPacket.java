package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.core.discovery.strategy.DictionaryStrategy;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Request discovery records from server: C -> S.
 * Only sent when the server's discovery strategy is DICTIONARY.
 */
public record RequestDictionaryDiscoveryFullPacket() implements Packet {
    public static final Packet.Factory<RequestDictionaryDiscoveryFullPacket> FACTORY = RequestDictionaryDiscoveryFullPacket::new;

    private RequestDictionaryDiscoveryFullPacket(FriendlyByteBuf buf) {
        this();
    }

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        ServerPlayer player = ctx.player();
        ServerWorldSession session = ServerWorldSession.get();
        if (session == null) {
            return;
        }
        if (session.getDiscoveryManager().getStrategy() instanceof DictionaryStrategy strategy) {
            ServerNetManager.replyDictionaryDiscoveryRecords(player, strategy.getAllRecords(player));
            LOGGER.info("Full discovery records sent to player {}.", EntityUtils.getNameString(player));
        } else {
            LOGGER.warn("Wrong discovery strategy from client of player {}. Ignored.", EntityUtils.getNameString(player));
        }
    }
}
