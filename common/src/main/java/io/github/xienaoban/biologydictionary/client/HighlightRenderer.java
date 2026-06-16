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
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@ClientOnly
public final class HighlightRenderer {
	private HighlightRenderer() {}

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
			submitEntity(entity, entityRenderDispatcher, deltaTracker, tickRateManager,
					camera, poseStack, levelRenderState, submitNodeCollector);
		}
		for (HighlightManager.HighlightedBlock highlighted : highlightManager.getHighlightedBlocks()) {
			// TODO: restore highlighted block rendering with the 26.1.2 block model submit API.
		}
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

}
