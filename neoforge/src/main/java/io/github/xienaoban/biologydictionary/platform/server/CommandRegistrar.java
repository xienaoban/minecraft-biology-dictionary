package io.github.xienaoban.biologydictionary.platform.server;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.xienaoban.biologydictionary.server.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class CommandRegistrar {
    private CommandRegistrar() {}

    public static void register() {
        for (LiteralArgumentBuilder<CommandSourceStack> command : Commands.ENTRIES) {
            NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> event.getDispatcher().register(command));
        }
    }
}
