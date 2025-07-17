package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.MinecraftUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.screen.EntityDetailScreen;
import io.github.xienaoban.biologydictionary.gui.screen.HomeScreen;
import io.github.xienaoban.biologydictionary.gui.screen.misc.BeehiveScreen;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
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

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;
import static io.github.xienaoban.biologydictionary.BiologyDictionaryClient.BDC;

@Environment(EnvType.CLIENT)
public final class BiologyDictionaryEvent {
    public static void openBookScreen(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        BDC.setHitEntity(null);
        BDC.setHitBlock(null);
        BDC.setHitEntityProperties(null);
        if (player == null) {
            LOGGER.error("Client player is null. Fail to open the Bole Screen.");
            return;
        }
        Entity target;
        float y = player.getXRot();
        HitResult hit = minecraft.hitResult;
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
            HighlightManager.addHighlightedBlock(minecraft.level, pos, 10 * 20); // todo delete
            @SuppressWarnings("all") // 'Level' used without 'try'-with-resources statement
            BlockState blockState = player.level().getBlockState(pos);
            if (blockState.getBlock() instanceof BeehiveBlock) {
                ClientNetManager.requestBeehiveInfo(pos);
                MinecraftUtils.setScreen(minecraft, new BeehiveScreen(pos));
                new ScreenRenderingContext(null).playScreenSound(SoundEvents.HONEYCOMB_WAX_ON, 1.0F, 0.8F);
                return;
            }
            target = null;
        } else {
            // Should not reach here, theoretically.
            target = null;
        }

        if (target == null) {
            MinecraftUtils.setScreen(minecraft, new HomeScreen());
        } else {
            EntityProperties<Entity> properties = new EntityProperties<>(target);
            BDC.setHitEntity(target);
            HighlightManager.addHighlightedEntity(target, 10 * 20); // todo delete
            BDC.setHitEntityProperties(properties);
            ClientNetManager.requestEntityData(target);
            try {
                MinecraftUtils.setScreen(minecraft, new EntityDetailScreen(properties));
            } catch (RuntimeException e) {
                Misc.printThrowableToLoggerAndGame(e);
                return;
            }
        }
        new ScreenRenderingContext(null).playScreenSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 0.8F);
    }
}
