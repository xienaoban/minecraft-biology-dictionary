package io.github.xienaoban.biologydictionary.mixin.rendering;

import io.github.xienaoban.biologydictionary.client.SilhouetteFogBuffer;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.CommonScreen;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Renders undiscovered entities as flat silhouettes in the GUI by swapping the shader fog
 * around the picture-in-picture {@code prepare} call. {@code prepare} spans both the submit
 * ({@code renderToTexture}) and the actual draw ({@code renderAllFeatures}), so wrapping it
 * keeps the silhouette fog live at draw time — when {@code entity.fsh} samples the FOG
 * uniform. Undiscovered entities are detected via the existing {@code renderState.outlineColor}
 * signal set in {@code ScreenRenderingContext#renderEntity}, scoped to Biology Dictionary screens.
 *
 * @see SilhouetteFogBuffer
 */
@ClientOnly
@Mixin(PictureInPictureRenderer.class)
public abstract class PictureInPictureRendererMixin {
    @Unique
    private static final String biologydictionary$PREPARE =
            "prepare(Lnet/minecraft/client/renderer/state/gui/pip/PictureInPictureRenderState;"
                    + "Lnet/minecraft/client/renderer/state/gui/GuiRenderState;"
                    + "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;I)V";

    @Inject(method = biologydictionary$PREPARE, at = @At("HEAD"))
    private void biologydictionary$beginSilhouetteFog(PictureInPictureRenderState renderState,
                                                     GuiRenderState guiRenderState,
                                                     FeatureRenderDispatcher featureRenderDispatcher,
                                                     int guiScale, CallbackInfo ci) {
        if (CommonScreen.isOpened()
                && renderState instanceof GuiEntityRenderState entityState
                && entityState.renderState().outlineColor != 0) {
            SilhouetteFogBuffer.beginSilhouette();
        }
    }

    @Inject(method = biologydictionary$PREPARE, at = @At("TAIL"))
    private void biologydictionary$endSilhouetteFog(PictureInPictureRenderState renderState,
                                                   GuiRenderState guiRenderState,
                                                   FeatureRenderDispatcher featureRenderDispatcher,
                                                   int guiScale, CallbackInfo ci) {
        SilhouetteFogBuffer.endSilhouette();
    }
}
