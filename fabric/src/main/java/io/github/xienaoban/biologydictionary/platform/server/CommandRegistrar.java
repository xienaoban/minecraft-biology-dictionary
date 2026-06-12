package io.github.xienaoban.biologydictionary.platform.server;

import io.github.xienaoban.biologydictionary.server.Commands;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class CommandRegistrar {
	private CommandRegistrar() {}

	public static void register() {
		for (Commands.CommandConsumer command : Commands.REGISTRATIONS) {
			CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> command.accept(dispatcher));
		}
	}
}
