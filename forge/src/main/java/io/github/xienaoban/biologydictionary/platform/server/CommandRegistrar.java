package io.github.xienaoban.biologydictionary.platform.server;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.xienaoban.biologydictionary.server.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class CommandRegistrar {
    private CommandRegistrar() {}

    public static void register() {
        for (LiteralArgumentBuilder<CommandSourceStack> command : Commands.ENTRIES) {
            MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> event.getDispatcher().register(command));
        }
    }
}
