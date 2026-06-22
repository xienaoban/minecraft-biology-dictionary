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
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

@ClientOnly
public final class HighlightRenderer {
    private static final int FULL_BRIGHT = 15728880;
    private static final int[] NO_TINT = new int[0];

    public static void submit(Minecraft client, EntityRenderDispatcher entityRenderDispatcher,
                              PoseStack poseStack, LevelRenderState levelRenderState,
                              SubmitNodeCollector submitNodeCollector) {
        ClientWorldSession cws = ClientWorldSession.get();
        if (cws == null) { return; }
        HighlightManager highlightManager = cws.getHighlightManager();
        if (!highlightManager.hasAnyHighlighted()) { return; }

        DeltaTracker deltaTracker = client.getDeltaTracker();
        TickRateManager tickRateManager = ClientUtils.getClientLevel(client).tickRateManager();
        Vec3 camera = levelRenderState.cameraRenderState.pos;
        for (HighlightManager.HighlightedEntity highlighted : highlightManager.getHighlightedEntities()) {
            Entity entity = highlighted.getEntity();
            if (shouldSkipEntity(client, entity)) {
                continue;
            }
            submitEntity(entity, entityRenderDispatcher, deltaTracker, tickRateManager,
                    camera, poseStack, levelRenderState, submitNodeCollector);
        }
        for (HighlightManager.HighlightedBlock highlighted : highlightManager.getHighlightedBlocks()) {
            BlockPos blockPos = highlighted.getBlockPos();
            BlockState blockState = highlighted.getBlockState();
            submitBlock(client, blockPos, blockState, camera, poseStack, submitNodeCollector);
        }
    }

    private static boolean shouldSkipEntity(Minecraft client, Entity entity) {
        return entity == client.player && ClientUtils.isFirstPerson();
    }

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

    private static void submitBlock(Minecraft client, BlockPos blockPos, BlockState blockState,
                                    Vec3 camera, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        BlockStateModel model = client.getModelManager().getBlockStateModelSet().get(blockState);
        List<BlockStateModelPart> modelParts = new ArrayList<>();
        RandomSource random = RandomSource.create(blockState.getSeed(blockPos));
        model.collectParts(random, modelParts);
        if (modelParts.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(blockPos.getX() - camera.x() + 0.001F, blockPos.getY() - camera.y() + 0.001F,
                blockPos.getZ() - camera.z() + 0.001F);
        poseStack.scale(0.998F, 0.998F, 0.998F);

        submitBlockModelLayer(submitNodeCollector, poseStack, modelParts,
                ChunkSectionLayer.SOLID, RenderTypes.solidMovingBlock());
        submitBlockModelLayer(submitNodeCollector, poseStack, modelParts,
                ChunkSectionLayer.CUTOUT, RenderTypes.cutoutMovingBlock());
        submitBlockModelLayer(submitNodeCollector, poseStack, modelParts,
                ChunkSectionLayer.TRANSLUCENT, RenderTypes.translucentMovingBlock());

        poseStack.popPose();
    }

    private static void submitBlockModelLayer(SubmitNodeCollector submitNodeCollector, PoseStack poseStack,
                                              List<BlockStateModelPart> modelParts, ChunkSectionLayer layer,
                                              RenderType renderType) {
        List<BlockStateModelPart> filtered = modelParts.stream()
                .map(part -> new LayerFilteredBlockStateModelPart(part, layer))
                .filter(LayerFilteredBlockStateModelPart::hasQuads)
                .map(BlockStateModelPart.class::cast)
                .toList();
        if (!filtered.isEmpty()) {
            submitNodeCollector.submitBlockModel(poseStack, renderType, filtered,
                    NO_TINT, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, Colors.HIGHLIGHT_DEFAULT_COLOR);
        }
    }

    private record LayerFilteredBlockStateModelPart(BlockStateModelPart delegate,
                                                    ChunkSectionLayer layer) implements BlockStateModelPart {
        @Override
        public List<BakedQuad> getQuads(Direction direction) {
            return delegate.getQuads(direction).stream()
                    .filter(quad -> quad.materialInfo().layer() == layer)
                    .toList();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return delegate.useAmbientOcclusion();
        }

        @Override
        public Material.Baked particleMaterial() {
            return delegate.particleMaterial();
        }

        @Override
        public int materialFlags() {
            return delegate.materialFlags();
        }

        private boolean hasQuads() {
            for (Direction direction : Direction.values()) {
                if (!getQuads(direction).isEmpty()) {
                    return true;
                }
            }
            return !getQuads(null).isEmpty();
        }
    }
}
