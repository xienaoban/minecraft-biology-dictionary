package io.github.xienaoban.biologydictionary.platform.server;

import io.github.xienaoban.biologydictionary.server.ServerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class ServerEventRegistrar {
	private ServerEventRegistrar() {}

	public static void register() {
		for (ServerEvents.ServerListener listener : ServerEvents.STARTED) {
			ServerLifecycleEvents.SERVER_STARTED.register(listener::run);
		}
		for (ServerEvents.ServerListener listener : ServerEvents.STOPPING) {
			ServerLifecycleEvents.SERVER_STOPPING.register(listener::run);
		}
		for (ServerEvents.PlayerListener listener : ServerEvents.PLAYER_LOGGED_IN) {
			ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> listener.run(handler.getPlayer()));
		}
	}
}
