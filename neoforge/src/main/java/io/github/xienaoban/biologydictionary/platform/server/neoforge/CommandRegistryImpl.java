package io.github.xienaoban.biologydictionary.platform.server.neoforge;

import io.github.xienaoban.biologydictionary.platform.server.CommandRegistry;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;

public final class CommandRegistryImpl {
    private static final List<CommandRegistry.CommandConsumer> consumers = new ArrayList<>();

    public static void registerCommand(CommandRegistry.CommandConsumer consumer) {
        consumers.add(consumer);
    }

    static {
        NeoForge.EVENT_BUS.addListener(CommandRegistryImpl::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        for (CommandRegistry.CommandConsumer consumer : consumers) {
            consumer.accept(event.getDispatcher());
        }
    }
}
