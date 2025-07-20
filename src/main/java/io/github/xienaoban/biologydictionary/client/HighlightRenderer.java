package io.github.xienaoban.biologydictionary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.biologydictionary.mixin.EntityRenderDispatcherIMixin;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class HighlightRenderer {
    public static void render(Minecraft client, ClientLevel level,
                              PoseStack poseStack, OutlineBufferSource bufferSource,
                              Camera camera, DeltaTracker deltaTracker) {
        bufferSource.setColor(/* R */ 0, /* G */ 255, /* B */ 0, /* A */ 255);

        EntityRenderDispatcher entityRenderDispatcher = client.getEntityRenderDispatcher();
        BlockRenderDispatcher blockRenderDispatcher = client.getBlockRenderer();

        Vec3 cameraVec = camera.getPosition();

        if (HighlightManager.hasAnyHighlighted()) {
            for (HighlightManager.HighlightedEntity ei : HighlightManager.getHighlightedEntities()) {
                Entity entity = ei.getEntity();
                float delta = deltaTracker.getGameTimeDeltaPartialTick(!level.tickRateManager().isEntityFrozen(entity));
                renderEntity(entityRenderDispatcher, poseStack, bufferSource, entity, cameraVec, delta);
            }
            for (HighlightManager.HighlightedBlock bi : HighlightManager.getHighlightedBlocks()) {
                BlockPos blockPos = bi.getBlockPos();
                BlockState blockState = bi.getBlockState();
                renderBlock(blockRenderDispatcher, poseStack, bufferSource, blockPos, blockState, cameraVec);
            }
        }
    }

    private static void renderEntity(EntityRenderDispatcher entityRenderDispatcher,
                                     PoseStack poseStack, MultiBufferSource bufferSource,
                                     Entity entity, Vec3 cameraVec, float delta) {
        double fixedX = Mth.lerp(delta, entity.xOld, entity.getX());
        double fixedY = Mth.lerp(delta, entity.yOld, entity.getY());
        double fixedZ = Mth.lerp(delta, entity.zOld, entity.getZ());
        boolean old = ((EntityRenderDispatcherIMixin) entityRenderDispatcher).getShouldRenderShadow();
        entityRenderDispatcher.setRenderShadow(false);
        entityRenderDispatcher.render(entity,
                fixedX - cameraVec.x(), fixedY - cameraVec.y(), fixedZ - cameraVec.z(), delta,
                poseStack, bufferSource, entityRenderDispatcher.getPackedLightCoords(entity, delta));
        entityRenderDispatcher.setRenderShadow(old);
    }

    private static void renderBlock(BlockRenderDispatcher blockRenderDispatcher,
                                    PoseStack poseStack, MultiBufferSource bufferSource,
                                    BlockPos blockPos, BlockState blockState, Vec3 cameraVec) {
        poseStack.pushPose();
        poseStack.scale(1.01F, 1.01F, 1.01F);
        poseStack.translate(blockPos.getX() - cameraVec.x(), blockPos.getY() - cameraVec.y(), blockPos.getZ() - cameraVec.z());
        blockRenderDispatcher.renderSingleBlock(blockState, poseStack, bufferSource, 0x00F00000, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
