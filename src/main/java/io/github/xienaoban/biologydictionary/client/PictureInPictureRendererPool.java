package io.github.xienaoban.biologydictionary.client;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.textures.GpuTexture;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.mixin.PictureInPictureRendererIMixin;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.gui.render.pip.*;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Borrowed from NeoForge: {@code net.neoforged.neoforge.client.gui.PictureInPictureRendererPool}.
 * A similar implementation.
 * <p>
 * And here's the comment from NeoForge:
 * <p>
 * Pools {@link PictureInPictureRenderer} for a single type of {@link PictureInPictureRenderState} and tries
 * to reuse renderers on subsequent frames.
 * <p>Vanilla only ever uses one PIP renderer per PIP state type. This can lead to crashes or
 * visual artifacts, since the backing render target textures are stored within the renderer,
 * and if two or more of the same type of state are submitted in one frame, the states will
 * begin interfering with each other.
 * <p>We solve this by using one renderer per distinct {@link PictureInPictureRenderState} state per frame,
 * and use this class to pool them for reuse in subsequent frames.
 */
public class PictureInPictureRendererPool<T extends PictureInPictureRenderState> implements AutoCloseable {

    public static Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererPool<?>> createFactories(List<PictureInPictureRenderer<?>> list) {
        ImmutableMap.Builder<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererPool<?>> builder = ImmutableMap.builder();

        for (PictureInPictureRenderer<?> pictureInPictureRenderer : list) {
            Class<? extends PictureInPictureRenderState> key = pictureInPictureRenderer.getRenderStateClass();

            Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<?>> factory = switch (pictureInPictureRenderer) {
                case GuiEntityRenderer          ignored -> buffers -> new GuiEntityRenderer(buffers, ClientUtils.getClient().getEntityRenderDispatcher());
                case GuiSkinRenderer            ignored -> GuiSkinRenderer::new;
                case GuiBookModelRenderer       ignored -> GuiBookModelRenderer::new;
                case GuiBannerResultRenderer    ignored -> GuiBannerResultRenderer::new;
                case GuiSignRenderer            ignored -> GuiSignRenderer::new;
                case GuiProfilerChartRenderer   ignored -> GuiProfilerChartRenderer::new;
                default -> null;
            };

            if (factory == null) { continue; }
            builder.put(key, new PictureInPictureRendererPool<>(Misc.cast(factory)));
        }

        return builder.buildOrThrow();
    }

    /**
     * Neo: This is used to check if this renderer can be reused for a given state, texture width and texture height on
     * a subsequent frame. In Vanilla, a renderer would be used for multiple different states even within the same frame,
     * leading to crashes and the last state being used for all blits of that renderer in that frame.
     */
    private static <T extends PictureInPictureRenderState> boolean canBeReusedFor(
            PictureInPictureRenderer<T> pictureInPictureRenderer, T state, int textureWidth, int textureHeight) {
        GpuTexture texture = ((PictureInPictureRendererIMixin) pictureInPictureRenderer).getTexture();
        return texture == null || (texture.getWidth(0) == textureWidth && texture.getHeight(0) == textureHeight);
    }

    private final Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<T>> factory;
    private Object2ObjectMap<T, PictureInPictureRenderer<T>> renderersLastFrame = new Object2ObjectOpenHashMap<>();
    // The renderers we already used in this frame, which we will try to reuse next frame
    private Object2ObjectMap<T, PictureInPictureRenderer<T>> renderersThisFrame = new Object2ObjectOpenHashMap<>();

    public PictureInPictureRendererPool(Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<T>> factory) {
        this.factory = factory;
    }

    public PictureInPictureRenderer<T> get(MultiBufferSource.BufferSource buffer, T state, int guiScale, boolean firstPass) {
        var width = (state.x1() - state.x0()) * guiScale;
        var height = (state.y1() - state.y0()) * guiScale;

        // On the first pass just try to reuse existing renderers by state equality
        if (firstPass) {
            var renderer = renderersLastFrame.get(state);
            if (renderer != null && canBeReusedFor(renderer, state, width, height)) {
                renderersLastFrame.remove(state);
                renderersThisFrame.put(state, renderer);
                return renderer;
            }
            return null;
        }

        // On the second pass, we try to find a renderer of matching texture size
        var it = renderersLastFrame.values().iterator();
        while (it.hasNext()) {
            var renderer = it.next();
            if (canBeReusedFor(renderer, state, width, height)) {
                it.remove();
                renderersThisFrame.put(state, renderer);
                return renderer;
            }
        }

        // Nothing else helped, create a new one
        var renderer = factory.apply(buffer);
        renderersThisFrame.put(state, renderer);
        return renderer;
    }

    public void clearUnusedRenderers() {
        renderersLastFrame.values().forEach(PictureInPictureRenderer::close);
        renderersLastFrame.clear();

        // Swap back/front buffer of maps, if you will
        var tmp = renderersLastFrame;
        renderersLastFrame = renderersThisFrame;
        renderersThisFrame = tmp;
    }

    @Override
    public void close() {
        renderersThisFrame.values().forEach(PictureInPictureRenderer::close);
        renderersLastFrame.values().forEach(PictureInPictureRenderer::close);
    }
}
