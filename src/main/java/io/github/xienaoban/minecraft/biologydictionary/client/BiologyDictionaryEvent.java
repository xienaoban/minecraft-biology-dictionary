package io.github.xienaoban.minecraft.biologydictionary.client;

import io.github.xienaoban.minecraft.biologydictionary.gui.screen.EntityDetailScreen;
import io.github.xienaoban.minecraft.biologydictionary.gui.screen.HomeScreen;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.CommonScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;
import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionaryClient.BDC;

@Environment(EnvType.CLIENT)
public class BiologyDictionaryEvent {
    public static void openBookScreen(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        BDC.setHitEntity(null);
        BDC.setHitBlock(null);
        if (player == null) {
            LOGGER.error("Client player is null. Fail to open the Bole Screen.");
            return;
        }
        Entity target;
        float y = player.getXRot();
        HitResult hit = minecraft.hitResult;
        if (y < -0.996F * 90.0F) target = null;
        else if (y > 0.996F * 90.0F) target = player;
        else if (y > 0.822F * 90.0F && player.isPassenger()) target = player.getVehicle();
        else if (hit == null) target = null;
        else if (hit.getType() != HitResult.Type.ENTITY) {
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hit).getBlockPos();
                BDC.setHitBlock(pos);
                BlockState blockState = player.level().getBlockState(pos);
                if (blockState.getBlock() instanceof BeehiveBlock) {
                    // todo
                    return;
                }
            }
            target = null;
        } else target = ((EntityHitResult) hit).getEntity();

        if (target == null) {
            minecraft.setScreen(new HomeScreen());
        } else {
            BDC.setHitEntity(target);
            minecraft.setScreen(new EntityDetailScreen(target));
        }
        CommonScreen.playScreenSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 0.8F);
    }
}
