package io.github.xienaoban.biologydictionary.mixin.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.biologydictionary.client.HighlightRenderer;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ClientOnly
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	@Shadow @Final private Minecraft minecraft;

	@Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

	@Inject(method = "extractVisibleEntities(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V",
			at = @At(value = "TAIL"))
	private void biologydictionary$setGlowing(Camera camera, Frustum frustum, DeltaTracker deltaTracker,
											  LevelRenderState levelRenderState, CallbackInfo ci) {
		ClientWorldSession cws = ClientWorldSession.get();
		if (cws != null && cws.getHighlightManager().hasAnyHighlighted()) {
			levelRenderState.haveGlowingEntities = true;
		}
	}

	@Inject(method = "submitEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
			at = @At(value = "TAIL"))
	private void biologydictionary$submitHighlight(PoseStack poseStack, LevelRenderState levelRenderState,
												   SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
		HighlightRenderer.submit(minecraft, entityRenderDispatcher, poseStack, levelRenderState, submitNodeCollector);
	}
}
