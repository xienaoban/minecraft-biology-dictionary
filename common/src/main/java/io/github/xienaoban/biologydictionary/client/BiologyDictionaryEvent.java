package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.gui.screen.BdEntityDetailScreen;
import io.github.xienaoban.biologydictionary.gui.screen.BdHomeScreen;
import io.github.xienaoban.biologydictionary.gui.screen.misc.BeehiveScreen;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;
import static io.github.xienaoban.biologydictionary.BiologyDictionaryClient.BDC;

@ClientOnly
public final class BiologyDictionaryEvent {
    public static void openBookScreen(Minecraft client) {
        LocalPlayer player = ClientUtils.getClientPlayer(client);
        try {
            resetHit();
            if (hasPermissionToOpenBook(player)) {
                openBookScreen0(client, player);
            } else {
                ClientUtils.sendCenteredMessage(TextUtils.translate(Lang.TEXT_NO_BIOLOGY_DICTIONARY_BOOK).withStyle(ChatFormatting.YELLOW));
            }
        } catch (Throwable e) {
            resetHit();
            BiologyDictionaryClient.printThrowableToLoggerAndGame("Failed to open Biology Dictionary screen!", e);
        }
    }

    private static void openBookScreen0(Minecraft client, LocalPlayer player) {
        if (player == null) {
            LOGGER.error("Client player is null. Fail to open the Bole Screen.");
            return;
        }
        Entity target;
        float y = player.getXRot();
        HitResult hit = client.hitResult;
        if (y < -0.996F * 90.0F) {
            target = null;
        } else if (y > 0.996F * 90.0F) {
            target = player;
        } else if (y > 0.822F * 90.0F && player.isPassenger()) {
            target = player.getVehicle();
        } else if (y > 0.666F * 90.0F && player.isPassenger()
                && (hit == null || hit.getType() != HitResult.Type.ENTITY)) {
            target = player.getVehicle();
        } else if (hit == null || hit.getType() == HitResult.Type.MISS) {
            target = null;
        } else if (hit.getType() == HitResult.Type.ENTITY) {
            target = ((EntityHitResult) hit).getEntity();
        } else if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            BDC.setHitBlock(pos);
            BlockState blockState = EntityUtils.getLevel(player).getBlockState(pos);
            if (blockState.getBlock() instanceof BeehiveBlock) {
                ClientNetManager.requestBeehiveInfo(pos);
                ClientUtils.setScreen(client, new BeehiveScreen(pos));
                ClientUtils.playScreenSound(client, SoundEvents.HONEYCOMB_WAX_ON, 1.0F, 0.8F);
                return;
            }
            target = null;
        } else {
            // Should not reach here, theoretically.
            target = null;
        }

        if (target == null) {
            ClientUtils.setScreen(client, new BdHomeScreen());
        } else {
            EntityProperties<Entity> properties = new EntityProperties<>(target);
            BDC.setHitEntity(target);
            BDC.setHitEntityProperties(properties);
            ClientNetManager.requestEntityData(target, true);
            ClientWorldSession.get().getDiscoveryClientCache().onEntityDetailScreenOpened(player, target);
            ClientUtils.setScreen(client, new BdEntityDetailScreen(properties));
        }
        ClientUtils.playScreenSound(client, SoundEvents.BOOK_PAGE_TURN, 1.0F, 0.8F);
    }

    private static void resetHit() {
        BDC.setHitEntity(null);
        BDC.setHitBlock(null);
        BDC.setHitEntityProperties(null);
    }

    private static boolean hasBook(LocalPlayer player) {
        return InventoryUtils.hasEnoughItems(PlayerUtils.getInventory(player), BiologyDictionaryItem.createBook(),
                (is1, is2) -> BiologyDictionaryItem.isBook(is2));
    }

    private static boolean hasPermissionToOpenBook(LocalPlayer player) {
        return PlayerUtils.isCreative(player)
                || (!ConfigsManager.getServer().isBookItemRequired())
                || hasBook(player);
    }
}
