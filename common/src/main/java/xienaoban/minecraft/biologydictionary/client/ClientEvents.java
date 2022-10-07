package xienaoban.minecraft.biologydictionary.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import xienaoban.minecraft.biologydictionary.BiologyDictionary;
import xienaoban.minecraft.biologydictionary.BiologyDictionaryClient;
import xienaoban.minecraft.biologydictionary.network.ClientNetworkManager;
import xienaoban.minecraft.biologydictionary.util.Keys;
import xienaoban.minecraft.biologydictionary.util.Permissions;
import xienaoban.minecraft.biologydictionary.util.Resources;
import xienaoban.minecraft.biologydictionary.util.WrappedClientApis;

import static xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;

@Environment(EnvType.CLIENT)
public class ClientEvents {
    private ClientEvents() { throw new AssertionError(); }

    public static void onOpenBiologyDictionary(Minecraft minecraft) {
        BiologyDictionary runtime = BiologyDictionary.get();
        BiologyDictionaryClient runtimeClient = BiologyDictionaryClient.get();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            LOGGER.error("Local player is null. Failed to open the screen.");
            return;
        }
        if (!Permissions.hasPermissionToOpenScreensByHotkey(player)) {
            Component text = Component.translatable(Keys.TEXT_SERVER_BAN_HOTKEY).withStyle(ChatFormatting.GOLD);
            player.displayClientMessage(text, false);
        }
        Entity target;
        float y = player.getXRot();
        LOGGER.info("aaaaaaaaaaaa " + player.getXRot());
        LOGGER.info(Resources.HORSE_INVENTORY_LOCATION.toString());
        HitResult hit = minecraft.hitResult;
        if (y < -88) target = null;
        else if (y > 88) target = player;
        else if (y > 70 && player.isPassenger()) target = player.getVehicle();
        else if (hit == null) target = null;
        else if (hit.getType() != HitResult.Type.ENTITY) {
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hit).getBlockPos();
                runtimeClient.setHitBlock(pos);
                BlockState blockState = player.level.getBlockState(pos);
                if (blockState.getBlock() instanceof BeehiveBlock) {
                    onOpenBeehiveScreen(pos);
                    return;
                }
            }
            target = null;
        }
        else target = ((EntityHitResult) hit).getEntity();

        if (target == null) onOpenBoleScreen();
        else {
            runtimeClient.setHitEntity(target);
            onOpenBoleScreen(target);
        }
        WrappedClientApis.playUiSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 0.8F);
    }

    private static void onOpenBoleScreen() {
        System.out.println("onOpenBoleScreen()");
        ClientNetworkManager.requestBoleScreen();
    }

    private static void onOpenBoleScreen(Entity target) {
        System.out.println("onOpenBoleScreen(" + target.getType().getDescriptionId() + ")");
        ClientNetworkManager.requestBoleScreen(target);
    }

    private static void onOpenBeehiveScreen(BlockPos pos) {
        System.out.println("onOpenBeehiveScreen(pos)");
        ClientNetworkManager.requestBeehiveScreen(pos);
    }
}
