package io.github.xienaoban.biologydictionary.platform.net;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ServerNetApiImpl implements ServerNetApi.PlatformBridge {
	@Override
	public void send(ServerPlayer player, Packet payload) {
		PacketDistributor.sendToPlayer(player, payload);
	}
}
