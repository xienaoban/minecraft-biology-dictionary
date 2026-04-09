package io.github.xienaoban.biologydictionary.platform.server;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.commands.CommandSourceStack;

public final class CommandRegistry {
    @ExpectPlatform
    public static void registerCommand(CommandConsumer consumer) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface CommandConsumer {
        void accept(CommandDispatcher<CommandSourceStack> dispatcher);
    }
}
