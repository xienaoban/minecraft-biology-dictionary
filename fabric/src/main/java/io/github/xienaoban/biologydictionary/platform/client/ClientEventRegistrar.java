package io.github.xienaoban.biologydictionary.platform.client;

import io.github.xienaoban.biologydictionary.client.ClientEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

@Environment(EnvType.CLIENT)
public final class ClientEventRegistrar {
	private ClientEventRegistrar() {}

	public static void register() {
		for (ClientEvents.ClientListener listener : ClientEvents.STARTED) {
			ClientLifecycleEvents.CLIENT_STARTED.register(listener::run);
		}
		for (ClientEvents.ClientListener listener : ClientEvents.STOPPING) {
			ClientLifecycleEvents.CLIENT_STOPPING.register(listener::run);
		}
		for (ClientEvents.ClientListener listener : ClientEvents.WORLD_CONNECTED) {
			ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> listener.run(client));
		}
		for (ClientEvents.ClientListener listener : ClientEvents.WORLD_DISCONNECTING) {
			ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> listener.run(client));
		}
		for (ClientEvents.ClientListener listener : ClientEvents.END_TICK) {
			ClientTickEvents.END_CLIENT_TICK.register(listener::run);
		}
	}
}
