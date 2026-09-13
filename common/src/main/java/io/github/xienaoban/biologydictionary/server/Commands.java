package io.github.xienaoban.biologydictionary.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.biologydictionary.net.payload.SendEntityOverviewScreenPacket;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.PlatformEntry;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.EntityType;

import java.util.List;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public final class Commands {
    @PlatformEntry
    public static final List<LiteralArgumentBuilder<CommandSourceStack>> ENTRIES = List.of(
            net.minecraft.commands.Commands.literal(BiologyDictionary.MOD_ID)
                .then(net.minecraft.commands.Commands.literal("config")
                        .then(net.minecraft.commands.Commands.literal("reload")
                                .requires(source -> source.permissions()
                                        .hasPermission(Permissions.COMMANDS_ADMIN))
                                .executes(Commands::reloadConfig)))
                .then(net.minecraft.commands.Commands.literal("overview")
                        .then(net.minecraft.commands.Commands.argument("entity_type", IdentifierArgument.id())
                                .executes(Commands::openOverview))));

    private Commands() {}

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        try {
            ConfigsManager.load();
            ConfigsManager.onUpdated();
            context.getSource().sendSuccess(() -> TextUtils.withFallbacks(
                    TextUtils.translate(Lang.TEXT_SERVER_CONFIGS_RELOAD_SUCCESS)), true);
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
        ServerWorldSession sws = ServerWorldSession.get();
        if (sws == null) {
            return 0;
        }

        Identifier entityTypeId = IdentifierArgument.getId(context, "entity_type");
        EntityType<?> entityType = EntityUtils.getEntityType(entityTypeId);
        if (entityType == null) {
            player.sendSystemMessage(TextUtils.withFallbacks(TextUtils.modLog(
                    TextUtils.translate(Lang.TEXT_UNKNOWN_ENTITY_TYPE))));
            return 0;
        }

        if (!sws.getDiscoveryManager().isDiscovered(player, entityType)) {
            player.sendSystemMessage(TextUtils.withFallbacks(TextUtils.modLog(
                    TextUtils.translate(Lang.TEXT_ENTITY_NOT_DISCOVERED))));
            return 0;
        }

        if (!ServerNetApi.canSend(player, SendEntityOverviewScreenPacket.class)) {
            player.sendSystemMessage(TextUtils.withFallbacks(TextUtils.modLog(
                    TextUtils.translate(Lang.TEXT_OVERVIEW_REQUIRES_CLIENT_MOD))));
            return 0;
        }

        ServerNetManager.sendEntityOverviewScreen(player, entityType);
        return Command.SINGLE_SUCCESS;
    }
}
