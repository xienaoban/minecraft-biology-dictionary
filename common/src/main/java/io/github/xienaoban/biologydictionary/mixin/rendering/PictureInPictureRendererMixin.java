package io.github.xienaoban.biologydictionary.mixin.rendering;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.CommonScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ClientOnly
@Mixin(net.minecraft.client.gui.render.pip.PictureInPictureRenderer.class)
public class PictureInPictureRendererMixin {
	@Inject(method = "prepare",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V",
					shift = At.Shift.AFTER))
	private void biologydictionary$flushOutlineBuffer(PictureInPictureRenderState renderState, GuiRenderState guiRenderState,
													 int scale, CallbackInfo ci) {
		if (!CommonScreen.isOpened()) {
			return;
		}
		((FeatureRenderDispatcherIMixin) Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher())
				.biologydictionary$getOutlineBufferSource()
				.endOutlineBatch();
	}
}
