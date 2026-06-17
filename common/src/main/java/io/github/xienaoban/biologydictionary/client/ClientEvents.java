package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.PlatformEntry;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.client.Minecraft;

import java.util.List;

@ClientOnly
public final class ClientEvents {
	@PlatformEntry
	public static final List<ClientListener> STARTED = List.of();

	@PlatformEntry
	public static final List<ClientListener> STOPPING = List.of();

	@PlatformEntry
	public static final List<ClientListener> WORLD_CONNECTED = List.of(client -> {
		WorldSession.init(ClientUtils.getClientLevel(client));
		ClientWorldSession.init();
		// Only request server configs from remote servers, not local servers.
		if (!ClientUtils.isLocalServer(client)) {
			ClientNetManager.requestServerConfigs();
		}
	});

	@PlatformEntry
	public static final List<ClientListener> WORLD_DISCONNECTING = List.of(client -> {
		ClientWorldSession.deinit();
		WorldSession.deinit();
		ConfigsManager.setLocalServerConfigs();
	});

	@PlatformEntry
	public static final List<ClientListener> END_TICK = List.of(client -> {
		if (!client.isPaused()) {
			BiologyDictionaryClient.tick();
		}
		while (KeyMappings.OPEN_HANDBOOK.consumeClick()) {
			if (client.player != null) {
				BiologyDictionaryEvent.openBookScreen(client);
			}
		}
		ClientWorldSession cws = ClientWorldSession.get();
		if (cws != null) {
			cws.tick();
		}
	});

	private ClientEvents() {}

	@FunctionalInterface
	public interface ClientListener {
		void run(Minecraft client);
	}
}
