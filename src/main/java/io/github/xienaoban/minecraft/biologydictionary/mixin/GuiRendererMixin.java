package io.github.xienaoban.minecraft.biologydictionary.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.fix.PictureInPictureRendererFactory;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.CommonScreen;
import io.github.xienaoban.minecraft.biologydictionary.common.util.Misc;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Shadow
    @Final GuiRenderState renderState;

    @Shadow
    @Final private MultiBufferSource.BufferSource bufferSource;

    @Unique
    private Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererFactory<?>> pictureInPictureRendererFactories;

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/client/gui/render/state/GuiRenderState;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Ljava/util/List;)V")
    private void init(GuiRenderState guiRenderState, MultiBufferSource.BufferSource bufferSource, List<PictureInPictureRenderer<?>> list, CallbackInfo ci) {
        pictureInPictureRendererFactories = PictureInPictureRendererFactory.createFactories(list);
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", at = @At("TAIL"))
    private void injectRender(GpuBufferSlice gpuBufferSlice, CallbackInfo ci) {
        pictureInPictureRendererFactories.values().forEach(PictureInPictureRendererFactory::clearUnusedRenderers);
    }

    @Inject(method = "close()V", at = @At("TAIL"))
    private void injectClose(CallbackInfo ci) {
        this.pictureInPictureRendererFactories.values().forEach(PictureInPictureRendererFactory::close);
    }

    @Inject(method = "preparePictureInPictureState(Lnet/minecraft/client/gui/render/state/pip/PictureInPictureRenderState;I)V", at = @At("HEAD"), cancellable = true)
    private <T extends PictureInPictureRenderState> void injectPreparePictureInPictureState(T pictureInPictureRenderState, int guiScale, CallbackInfo ci) {
        if (CommonScreen.isOpened()) {
            PictureInPictureRendererFactory<T> factory = Misc.cast(pictureInPictureRendererFactories.get(pictureInPictureRenderState.getClass()));
            if (factory != null) {
                PictureInPictureRenderer<T> pictureinpicturerenderer = factory.create(bufferSource);
                if (pictureinpicturerenderer != null) {
                    pictureinpicturerenderer.prepare(pictureInPictureRenderState, renderState, guiScale);
                    ci.cancel();
                }
            }
        }
    }
}