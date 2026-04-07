package io.github.xienaoban.biologydictionary.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.platform.server.CommandRegistry;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;

public final class CommandManager {
    private CommandManager() {}

    public static void init() {
        CommandRegistry.registerCommand(CommandManager::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(BiologyDictionary.MOD_ID)
                .then(Commands.literal("config")
                .then(Commands.literal("reload")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                        .executes(CommandManager::reloadConfig))));
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        ConfigsManager.load();
        ConfigsManager.onUpdated();
        context.getSource().sendSuccess(() -> TextUtils.translate(Lang.TEXT_CONFIG_RELOAD_SUCCESS), true);
        return 1;
    }
}
