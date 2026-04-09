package io.github.xienaoban.biologydictionary.platform.server.fabric;

import io.github.xienaoban.biologydictionary.platform.server.CommandRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class CommandRegistryImpl {
    public static void registerCommand(CommandRegistry.CommandConsumer consumer) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                consumer.accept(dispatcher));
    }
}
