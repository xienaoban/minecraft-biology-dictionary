package io.github.xienaoban.biologydictionary.server;

import com.mojang.brigadier.CommandDispatcher;
import io.github.xienaoban.biologydictionary.platform.PlatformEntry;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;

public final class Commands {
	@PlatformEntry
	public static final List<CommandConsumer> REGISTRATIONS = List.of();

	private Commands() {}

	@FunctionalInterface
	public interface CommandConsumer {
		void accept(CommandDispatcher<CommandSourceStack> dispatcher);
	}
}
