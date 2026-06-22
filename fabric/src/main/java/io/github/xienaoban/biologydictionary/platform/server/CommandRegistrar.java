package io.github.xienaoban.biologydictionary.platform.server;

import io.github.xienaoban.biologydictionary.server.Commands;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class CommandRegistrar {
    private CommandRegistrar() {}

    public static void register() {
        for (var command : Commands.ENTRIES) {
            CommandRegistrationCallback.EVENT.register(
                    (dispatcher, registryAccess, environment) -> dispatcher.register(command));
        }
    }
}
