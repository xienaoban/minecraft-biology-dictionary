package io.github.xienaoban.biologydictionary.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.platform.PlatformEntry;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permissions;

import java.util.List;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public final class Commands {
	@PlatformEntry
	public static final List<LiteralArgumentBuilder<CommandSourceStack>> ENTRIES = List.of(
			net.minecraft.commands.Commands.literal(BiologyDictionary.MOD_ID)
				.then(net.minecraft.commands.Commands.literal("config")
				.then(net.minecraft.commands.Commands.literal("reload")
						.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
						.executes(Commands::reloadConfig))));

	private Commands() {}

	private static int reloadConfig(CommandContext<CommandSourceStack> context) {
		try {
			ConfigsManager.load();
			ConfigsManager.onUpdated();
			context.getSource().sendSuccess(() -> TextUtils.translate(Lang.TEXT_SERVER_CONFIGS_RELOAD_SUCCESS), true);
			return Command.SINGLE_SUCCESS;
		} catch (Exception e) {
			LOGGER.error("Failed to reload config!", e);
			return 0;
		}
	}
}
