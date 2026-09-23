package io.github.xienaoban.biologydictionary.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.ServerEntityOverviewManager;
import io.github.xienaoban.biologydictionary.net.payload.SendEntityOverviewPacket;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.server.CommandRegistry;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public final class CommandManager {
    private CommandManager() {}

    public static void init() {
        CommandRegistry.registerCommand(CommandManager::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(BiologyDictionary.MOD_ID)
                .then(Commands.literal("config")
                        .then(Commands.literal("reload")
                                .requires(source -> source.hasPermission(2))
                                .executes(CommandManager::reloadConfig)))
                .then(Commands.literal("overview")
                        .then(Commands.argument("entity_type", ResourceLocationArgument.id())
                                .executes(CommandManager::openOverview))));
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        try {
            ConfigsManager.load();
            ConfigsManager.onUpdated();
            ServerPlayer player = context.getSource().getPlayer();
            Component message = TextUtils.modLog(
                    TextUtils.translate(Lang.TEXT_SERVER_CONFIGS_RELOAD_SUCCESS));
            context.getSource().sendSuccess(() -> TextUtils.withFallbacks(message, player), true);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            LOGGER.error("Failed to reload config!", e);
            return 0;
        }
    }

    private static int openOverview(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        ResourceLocation entityTypeId = ResourceLocationArgument.getId(context, "entity_type");
        EntityType<?> entityType = EntityUtils.getEntityType(entityTypeId);
        if (entityType == null) {
            player.sendSystemMessage(TextUtils.withFallbacks(TextUtils.modLog(
                    TextUtils.translate(Lang.TEXT_UNKNOWN_ENTITY_TYPE)), player));
            return 0;
        }

        if (!ServerNetApi.canSend(player, SendEntityOverviewPacket.class)) {
            player.sendSystemMessage(TextUtils.withFallbacks(TextUtils.modLog(
                    createClientModRequiredMessage()), player));
            return 0;
        }

        if (!ServerEntityOverviewManager.send(player, entityType, true)) {
            player.sendSystemMessage(TextUtils.withFallbacks(TextUtils.modLog(
                    TextUtils.translate(Lang.TEXT_ENTITY_NOT_DISCOVERED)), player));
            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }

    private static Component createClientModRequiredMessage() {
        Component modName = TextUtils.translate(Lang.TEXT_MOD_NAME_WITH_BRACKETS).withStyle(ChatFormatting.GREEN);
        modName = TextUtils.withHoverShowText(modName, TextUtils.translate(Lang.TEXT_CLICK_TO_MODRINTH));
        modName = TextUtils.withClickOpenUrl(modName, BiologyDictionary.MODRINTH_PAGE);
        return TextUtils.translate(Lang.TEXT_OVERVIEW_REQUIRES_CLIENT_MOD, modName)
                .withStyle(ChatFormatting.YELLOW);
    }
}
