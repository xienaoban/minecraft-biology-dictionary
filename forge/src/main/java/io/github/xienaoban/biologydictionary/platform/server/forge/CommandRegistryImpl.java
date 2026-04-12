package io.github.xienaoban.biologydictionary.platform.server.forge;

import io.github.xienaoban.biologydictionary.platform.server.CommandRegistry;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public final class CommandRegistryImpl {
    private static final List<CommandRegistry.CommandConsumer> consumers = new ArrayList<>();

    public static void registerCommand(CommandRegistry.CommandConsumer consumer) {
        consumers.add(consumer);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        for (CommandRegistry.CommandConsumer consumer : consumers) {
            consumer.accept(event.getDispatcher());
        }
    }
}
