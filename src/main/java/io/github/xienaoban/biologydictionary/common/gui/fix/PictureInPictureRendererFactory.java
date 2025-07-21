package io.github.xienaoban.biologydictionary.common.gui.fix;

import com.google.common.collect.ImmutableMap;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import net.minecraft.client.gui.render.pip.*;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Borrowed from NeoForge: {@code net.neoforged.neoforge.client.gui.PictureInPictureRendererPool}.
 * Here's an easy one. I didn't implement the reuse logic since I found that the performance is not that poor.
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
public class PictureInPictureRendererFactory<T extends PictureInPictureRenderState> implements AutoCloseable {
    private final Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<T>> factory;
    private List<PictureInPictureRenderer<T>> renderersLastFrame = new ArrayList<>();
    private List<PictureInPictureRenderer<T>> renderersThisFrame = new ArrayList<>();

    public PictureInPictureRendererFactory(Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<T>> factory) {
        this.factory = factory;
    }

    public PictureInPictureRenderer<T> create(MultiBufferSource.BufferSource buffer) {
        // Nothing helped, create a new one
        PictureInPictureRenderer<T> renderer = factory.apply(buffer);
        renderersThisFrame.add(renderer);
        return renderer;
    }

    public void clearUnusedRenderers() {
        renderersLastFrame.forEach(PictureInPictureRenderer::close);
        renderersLastFrame.clear();

        // Swap back/front buffer of maps, if you will
        var tmp = renderersLastFrame;
        renderersLastFrame = renderersThisFrame;
        renderersThisFrame = tmp;
    }

    @Override
    public void close() {
        renderersThisFrame.forEach(PictureInPictureRenderer::close);
        renderersLastFrame.forEach(PictureInPictureRenderer::close);
    }

    public static Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererFactory<?>> createFactories(List<PictureInPictureRenderer<?>> list) {
        ImmutableMap.Builder<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererFactory<?>> builder = ImmutableMap.builder();

        for (PictureInPictureRenderer<?> pictureInPictureRenderer : list) {
            Class<? extends PictureInPictureRenderState> key = pictureInPictureRenderer.getRenderStateClass();

            Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<?>> factory = switch (pictureInPictureRenderer) {
                case GuiEntityRenderer          ignored -> buffers -> new GuiEntityRenderer(buffers, McClientUtils.getClient().getEntityRenderDispatcher());
                case GuiSkinRenderer            ignored -> GuiSkinRenderer::new;
                case GuiBookModelRenderer       ignored -> GuiBookModelRenderer::new;
                case GuiBannerResultRenderer    ignored -> GuiBannerResultRenderer::new;
                case GuiSignRenderer            ignored -> GuiSignRenderer::new;
                case GuiProfilerChartRenderer   ignored -> GuiProfilerChartRenderer::new;
                default -> null;
            };

            if (factory == null) { continue; }
            builder.put(key, new PictureInPictureRendererFactory<>(Misc.cast(factory)));
        }

        return builder.buildOrThrow();
    }
}
