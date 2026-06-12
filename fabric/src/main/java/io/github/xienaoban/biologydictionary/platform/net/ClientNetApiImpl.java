package io.github.xienaoban.biologydictionary.platform.net;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public final class ClientNetApiImpl implements ClientNetApi.PlatformBridge {
	@Override
	public void send(Packet payload) {
		ClientPlayNetworking.send(payload);
	}
}
