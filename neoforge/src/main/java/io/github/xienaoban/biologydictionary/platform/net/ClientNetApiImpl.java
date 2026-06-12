package io.github.xienaoban.biologydictionary.platform.net;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class ClientNetApiImpl implements ClientNetApi.PlatformBridge {
	@Override
	public void send(Packet payload) {
		ClientPacketDistributor.sendToServer(payload);
	}
}
