package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.core.discovery.strategy.BiologyDictionaryDiscoveryStrategy;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public record RequestBiologyDictionaryDiscoveryFullPacket() implements Packet {
	public static final Packet.Factory<RequestBiologyDictionaryDiscoveryFullPacket> FACTORY =
			RequestBiologyDictionaryDiscoveryFullPacket::new;

	private RequestBiologyDictionaryDiscoveryFullPacket(FriendlyByteBuf buf) {
		this();
	}

	@Override
	public void write(FriendlyByteBuf buf) {}

	@Override
	public void serverReceive(ServerNetApi.Context ctx) {
		ServerPlayer player = ctx.player();
		ServerWorldSession sws = ServerWorldSession.get();
		if (sws == null) {
			LOGGER.warn("Null ServerWorldSession. Ignored.", new RuntimeException());
			return;
		}

		if (sws.getDiscoveryManager().getStrategy() instanceof BiologyDictionaryDiscoveryStrategy strategy) {
			ServerNetManager.replyDictionaryDiscoveryRecords(player, strategy.getAllRecords(player));
			LOGGER.info("Full discovery records sent to player {}.", EntityUtils.getNameString(player));
		} else {
			LOGGER.warn("Wrong discovery strategy from client of player {}. Ignored.", EntityUtils.getNameString(player));
		}
	}
}
