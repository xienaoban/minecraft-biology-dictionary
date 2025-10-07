package io.github.xienaoban.biologydictionary.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import io.github.xienaoban.biologydictionary.client.PictureInPictureRendererPool;
import io.github.xienaoban.biologydictionary.common.gui.screen.CommonScreen;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * To fix the GUI rendering bug that only one entity can be rendered in a screen.
 * The solution is learned from NeoForge.
 *
 * @see PictureInPictureRendererPool
 */
@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Shadow
    @Final GuiRenderState renderState;

    @Shadow
    @Final private MultiBufferSource.BufferSource bufferSource;

    @Unique
    private Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererPool<?>> pictureInPictureRendererPools;
    @Unique
    private final Set<PictureInPictureRenderState> pictureInPictureRenderStatesScratch = new ReferenceOpenHashSet<>();

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/client/gui/render/state/GuiRenderState;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;Ljava/util/List;)V")
    private void init(GuiRenderState guiRenderState, MultiBufferSource.BufferSource bufferSource, SubmitNodeCollector submitNodeCollector, FeatureRenderDispatcher featureRenderDispatcher, List<PictureInPictureRenderer<?>> list, CallbackInfo ci) {
        pictureInPictureRendererPools = PictureInPictureRendererPool.createFactories(list);
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", at = @At("TAIL"))
    private void injectRender(GpuBufferSlice gpuBufferSlice, CallbackInfo ci) {
        pictureInPictureRendererPools.values().forEach(PictureInPictureRendererPool::clearUnusedRenderers);
    }

    @Inject(method = "close()V", at = @At("TAIL"))
    private void injectClose(CallbackInfo ci) {
        pictureInPictureRendererPools.values().forEach(PictureInPictureRendererPool::close);
    }

    @Inject(method = "preparePictureInPicture()V", at = @At("HEAD"), cancellable = true)
    private void injectPreparePictureInPicture(CallbackInfo ci) {
        if (CommonScreen.isOpened()) {
            int guiScale = Minecraft.getInstance().getWindow().getGuiScale();
            pictureInPictureRenderStatesScratch.clear();
            renderState.forEachPictureInPicture(state -> {
                if (preparePictureInPictureState(state, guiScale, true)) {
                    pictureInPictureRenderStatesScratch.add(state);
                }
            });
            renderState.forEachPictureInPicture(state -> {
                if (pictureInPictureRenderStatesScratch.add(state)) {
                    preparePictureInPictureState(state, guiScale, false);
                }
            });
            pictureInPictureRenderStatesScratch.clear();
            ci.cancel();
        }
    }

    @Unique
    private <T extends PictureInPictureRenderState> boolean preparePictureInPictureState(T pictureInPictureRenderState, int guiScale, boolean firstPass) {
        PictureInPictureRendererPool<T> pool = Misc.cast(pictureInPictureRendererPools.get(pictureInPictureRenderState.getClass()));
        if (pool == null) { return false; }
        PictureInPictureRenderer<T> pictureinpicturerenderer = pool.get(bufferSource, pictureInPictureRenderState, guiScale, firstPass);
        if (pictureinpicturerenderer != null) {
            pictureinpicturerenderer.prepare(pictureInPictureRenderState, renderState, guiScale);
            return true;
        }
        return false;
    }
}