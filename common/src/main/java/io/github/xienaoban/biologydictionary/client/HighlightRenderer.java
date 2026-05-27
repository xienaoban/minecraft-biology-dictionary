package io.github.xienaoban.biologydictionary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.RenderUtils;
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

@ClientOnly
public final class HighlightRenderer {
    public static void submit(Minecraft client, EntityRenderDispatcher entityRenderDispatcher,
                              PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector) {
        ClientWorldSession cws = ClientWorldSession.get();
        if (cws == null) { return; }
        HighlightManager hm = cws.getHighlightManager();
        if (!hm.hasAnyHighlighted()) { return; }

        DeltaTracker deltaTracker = client.getDeltaTracker();
        TickRateManager tickRateManager = ClientUtils.getClientLevel(client).tickRateManager();
        Vec3 camera = levelRenderState.cameraRenderState.pos;
        for (HighlightManager.HighlightedEntity ei : hm.getHighlightedEntities()) {
            Entity entity = ei.getEntity();
            submitEntity(entity, entityRenderDispatcher, deltaTracker, tickRateManager,
                    camera, poseStack, levelRenderState, submitNodeCollector);
        }
        for (HighlightManager.HighlightedBlock bi : hm.getHighlightedBlocks()) {
            BlockPos blockPos = bi.getBlockPos();
            BlockState blockState = bi.getBlockState();
            submitBlock(blockPos, blockState,
                    camera, poseStack, levelRenderState, submitNodeCollector);
        }
    }

    /**
     * @see net.minecraft.client.renderer.LevelRenderer#submitEntities(com.mojang.blaze3d.vertex.PoseStack, net.minecraft.client.renderer.state.LevelRenderState, net.minecraft.client.renderer.SubmitNodeCollector)
     * @see net.minecraft.client.gui.screens.inventory.InventoryScreen#renderEntityInInventoryFollowsMouse(net.minecraft.client.gui.GuiGraphics, int, int, int, int, int, float, float, float, net.minecraft.world.entity.LivingEntity)
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
     * @see net.minecraft.client.gui.screens.inventory.InventoryScreen#renderEntityInInventoryFollowsMouse(net.minecraft.client.gui.GuiGraphics, int, int, int, int, int, float, float, float, net.minecraft.world.entity.LivingEntity)
     */
    private static void submitBlock(BlockPos blockPos, BlockState blockState,
                                    Vec3 camera, PoseStack poseStack,
                                    LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector) {
        poseStack.pushPose();
        poseStack.translate(blockPos.getX() - camera.x() + 0.001F, blockPos.getY() - camera.y() + 0.001F, blockPos.getZ() - camera.z() + 0.001F);
        poseStack.scale(0.998F, 0.998F, 0.998F);

        submitNodeCollector.submitBlock(poseStack, blockState, 15728880, OverlayTexture.NO_OVERLAY, Colors.HIGHLIGHT_DEFAULT_COLOR);

        poseStack.popPose();
    }
}
