package io.github.xienaoban.biologydictionary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.common.util.RenderUtils;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class HighlightRenderer {
    public static void submit(Minecraft client, EntityRenderDispatcher entityRenderDispatcher,
                              PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector) {
        if (!HighlightManager.hasAnyHighlighted()) { return; }

        DeltaTracker deltaTracker = client.getDeltaTracker();
        TickRateManager tickRateManager = ClientUtils.getClientLevel(client).tickRateManager();
        Vec3 camera = levelRenderState.cameraRenderState.pos;

        for (HighlightManager.HighlightedEntity ei : HighlightManager.getHighlightedEntities()) {
            Entity entity = ei.getEntity();
            submitEntity(entity, entityRenderDispatcher, deltaTracker, tickRateManager,
                    camera, poseStack, levelRenderState, submitNodeCollector);
        }
        for (HighlightManager.HighlightedBlock bi : HighlightManager.getHighlightedBlocks()) {
            BlockPos blockPos = bi.getBlockPos();
            BlockState blockState = bi.getBlockState();
            submitBlock(blockPos, blockState,
                    camera, poseStack, levelRenderState, submitNodeCollector);
        }
    }

    /**
     * @see net.minecraft.client.renderer.LevelRenderer#submitEntities(com.mojang.blaze3d.vertex.PoseStack, net.minecraft.client.renderer.state.LevelRenderState, net.minecraft.client.renderer.SubmitNodeCollector)
     * @see net.minecraft.client.gui.screens.inventory.InventoryScreen#renderEntityInInventory(net.minecraft.client.gui.GuiGraphics, int, int, int, int, float, org.joml.Vector3f, org.joml.Quaternionf, org.joml.Quaternionf, net.minecraft.world.entity.LivingEntity)
     * @see net.minecraft.client.renderer.LevelRenderer#extractEntity(net.minecraft.world.entity.Entity, float)
     */
    private static void submitEntity(Entity entity, EntityRenderDispatcher entityRenderDispatcher,
                                     DeltaTracker deltaTracker, TickRateManager tickRateManager,
                                     Vec3 camera, PoseStack poseStack,
                                     LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector) {
        float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(entity));
        EntityRenderState entityRenderState = RenderUtils.createRenderState(entityRenderDispatcher, entity, tickDelta);
        RenderUtils.renderBodyOnly(entityRenderState);
        entityRenderState.outlineColor = Colors.HIGHLIGHT_DEFAULT_COLOR;
        entityRenderDispatcher.submit(
                        entityRenderState,
                        levelRenderState.cameraRenderState,
                        entityRenderState.x - camera.x(),
                        entityRenderState.y - camera.y(),
                        entityRenderState.z - camera.z(),
                        poseStack,
                        submitNodeCollector
                );
    }

    /**
     * @see net.minecraft.client.renderer.LevelRenderer#submitBlockEntities(com.mojang.blaze3d.vertex.PoseStack, net.minecraft.client.renderer.state.LevelRenderState, net.minecraft.client.renderer.SubmitNodeStorage)
     * @see net.minecraft.client.gui.screens.inventory.InventoryScreen#renderEntityInInventory(net.minecraft.client.gui.GuiGraphics, int, int, int, int, float, org.joml.Vector3f, org.joml.Quaternionf, org.joml.Quaternionf, net.minecraft.world.entity.LivingEntity)
     */
    private static void submitBlock(BlockPos blockPos, BlockState blockState,
                                    Vec3 camera, PoseStack poseStack,
                                    LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector) {
        poseStack.pushPose();
        poseStack.translate(blockPos.getX() - camera.x(), blockPos.getY() - camera.y(), blockPos.getZ() - camera.z());

        submitNodeCollector.submitBlock(poseStack, blockState, 15728880, OverlayTexture.NO_OVERLAY, Colors.HIGHLIGHT_DEFAULT_COLOR);

        poseStack.popPose();
    }
}
