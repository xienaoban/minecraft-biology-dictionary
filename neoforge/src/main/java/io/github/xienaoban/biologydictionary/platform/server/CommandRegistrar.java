package io.github.xienaoban.biologydictionary.platform.server;

import io.github.xienaoban.biologydictionary.server.Commands;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class CommandRegistrar {
	private CommandRegistrar() {}

	public static void register() {
		for (Commands.CommandConsumer command : Commands.REGISTRATIONS) {
			NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> command.accept(event.getDispatcher()));
		}
	}
}
