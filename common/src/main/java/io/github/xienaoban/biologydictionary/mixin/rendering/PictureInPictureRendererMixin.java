package io.github.xienaoban.biologydictionary.mixin.rendering;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.commands.RenderPass;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.CommonScreen;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher.PreparedFrame;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Executes Minecraft's outline phase inside the GUI picture-in-picture render pass.
 * Vanilla {@code renderAllFeatures} does not run the outline phase for picture-in-picture
 * rendering, so GUI entity outlines would otherwise be dropped. Biology Dictionary marks
 * undiscovered entities via {@code EntityRenderState#outlineColor}; reusing that phase
 * renders the normal vanilla silhouette without swapping global shader fog.
 *
 * @see net.minecraft.client.renderer.feature.FeatureRenderDispatcher.PreparedFrame#executeOutline(RenderPass)
 */
@ClientOnly
@Mixin(PictureInPictureRenderer.class)
public abstract class PictureInPictureRendererMixin {
    @Unique
    private static final String biologydictionary$PREPARE =
            "prepare(Lnet/minecraft/client/renderer/state/gui/pip/PictureInPictureRenderState;"
                    + "Lnet/minecraft/client/renderer/state/gui/GuiRenderState;"
                    + "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;I)V";

    @Inject(
            method = biologydictionary$PREPARE,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;"
                            + "renderAllFeatures(Lcom/mojang/renderpearl/api/commands/RenderPass;"
                            + "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;)V",
                    shift = At.Shift.AFTER))
    private void biologydictionary$executeGuiEntityOutline(PictureInPictureRenderState renderState,
                                                           GuiRenderState guiRenderState,
                                                           FeatureRenderDispatcher featureRenderDispatcher,
                                                           int guiScale,
                                                           CallbackInfo ci,
                                                           @Local PreparedFrame frame,
                                                           @Local RenderPass renderPass) {
        if (!CommonScreen.isOpened()
                || !(renderState instanceof GuiEntityRenderState entityState)
                || entityState.renderState().outlineColor == 0) {
            return;
        }
        frame.executeOutline(renderPass);
    }
}
