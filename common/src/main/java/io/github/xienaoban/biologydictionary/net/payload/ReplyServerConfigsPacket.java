package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Server configs sync packet: S -> C.
 * Sent in response to {@link RequestServerConfigsPacket}.
 */
public record ReplyServerConfigsPacket(String serverConfigsYaml) implements Packet {
	public static final Packet.Factory<ReplyServerConfigsPacket> FACTORY = ReplyServerConfigsPacket::new;

	private ReplyServerConfigsPacket(FriendlyByteBuf buf) {
		this(buf.readUtf());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeUtf(serverConfigsYaml);
	}

	@ClientOnly
	@Override
	public void clientReceive(ClientNetApi.Context ctx) {
		@ClientOnly final class CO { static void receive(ReplyServerConfigsPacket packet) {
			Configs.ServerConfigs remoteConfigs = new Configs.ServerConfigs();
			boolean success = ConfigsManager.deserializeConfigCategory(packet.serverConfigsYaml(), remoteConfigs);
			if (!success) {
				return;
			}
			ConfigsManager.setRemoteServerConfigs(remoteConfigs);
			ConfigsManager.onUpdated();
		}}
		CO.receive(this);
	}
}
