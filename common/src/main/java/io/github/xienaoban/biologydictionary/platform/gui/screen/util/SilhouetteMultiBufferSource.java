package io.github.xienaoban.biologydictionary.platform.gui.screen.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;

import java.util.Optional;

/**
 * This file is generated entirely by AI. The author (me) is completely unfamiliar with
 * rendering and has no ability to determine whether there are potential problems with
 * the following logic!
 *
 * <p>A {@link MultiBufferSource} wrapper that renders entities as solid-color silhouettes.</p>
 *
 * <h2>Background: How vanilla entity glow/outline rendering works</h2>
 *
 * <p>Minecraft's entity glow effect (the glowing outline you see on entities marked as
 * glowing, or on team-highlighted entities) uses a dedicated rendering pipeline:</p>
 *
 * <ol>
 *   <li>{@code OutlineBufferSource} wraps the normal {@code MultiBufferSource}. When an
 *       entity renderer calls {@code getBuffer(renderType)}, {@code OutlineBufferSource}
 *       produces a {@code VertexMultiConsumer} that writes to <b>two</b> consumers:
 *       one that discards the textured data (noop), and one that renders an outline
 *       using the {@code rendertype_outline} shader.</li>
 *   <li>The {@code rendertype_outline} shader produces <b>solid colors</b>: it uses
 *       the vertex color for RGB output, and only samples the texture for alpha
 *       masking (so transparent areas of the texture are not rendered). This is why
 *       the glow appears as a solid-color overlay, not a textured one.</li>
 *   <li>Outline render types have their output redirected to a separate framebuffer
 *       called {@code entityTarget} ({@code LevelRenderer.entityTarget()}), via the
 *       {@code RenderStateShard.OutlineProperty.OUTLINE_TARGET} output state.</li>
 *   <li>After entity rendering, {@code LevelRenderer.doEntityOutline()} blits
 *       {@code entityTarget} onto the main screen with blending, producing the
 *       visible glow effect.</li>
 * </ol>
 *
 * <h2>How this class works</h2>
 *
 * <p>This class repurposes the vanilla outline pipeline to produce silhouettes instead
 * of glows. The key idea is that by redirecting entity render types to their
 * <b>outline variants</b> and overriding the vertex color to a fixed value, we can
 * render any entity as a solid-color silhouette.</p>
 *
 * <p>Each {@link RenderType} has an {@code OutlineProperty} which determines whether
 * an outline variant exists:</p>
 * <ul>
 *   <li>{@code AFFECTS_OUTLINE}: The render type has an outline variant, accessible
 *       via {@code renderType.outline()}. The outline variant uses the
 *       {@code rendertype_outline} shader, writes to {@code entityTarget}, and has
 *       {@code NO_DEPTH_TEST}. Almost all entity body render types
 *       ({@code entitySolid}, {@code entityCutout}, {@code entityCutoutNoCull},
 *       {@code entityTranslucent}, etc.) have this property by default.</li>
 *   <li>{@code NONE}: No outline variant. {@code renderType.outline()} returns
 *       {@code Optional.empty()}. Examples: {@code entityNoOutline}, {@code eyes},
 *       {@code entityShadow}, {@code armorEntityGlint}, {@code leash()}.</li>
 *   <li>{@code IS_OUTLINE}: The render type <i>is itself</i> an outline type.
 *       {@code renderType.outline()} also returns {@code Optional.empty()}.</li>
 * </ul>
 *
 * <p>In {@link #getBuffer(RenderType)}:</p>
 * <ul>
 *   <li>If the render type has an outline variant, we obtain the outline render type
 *       and wrap its vertex consumer with {@link ColorOverrideConsumer}, which
 *       overrides the vertex color to the silhouette color while delegating UV
 *       coordinates (needed for alpha masking by the shader).</li>
 *   <li>If the render type has no outline variant, we return a {@link #NOOP}
 *       consumer that discards all vertex data. These parts (glints, shadows, leashes,
 *       etc.) are simply not rendered as part of the silhouette.</li>
 * </ul>
 *
 * <p>The outline variant's output state is {@code OUTLINE_TARGET}, so all silhouette
 * data is written to {@code entityTarget} — a separate framebuffer — rather than the
 * main screen. After entity rendering completes, {@link #end()} blits
 * {@code entityTarget} onto the main framebuffer with alpha blending.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * SilhouetteMultiBufferSource silhouette = new SilhouetteMultiBufferSource(color);
 * entityRenderDispatcher.render(entity, x, y, z, yaw, partialTick, poseStack, silhouette, light);
 * guiGraphics.flush();
 * silhouette.end();
 * }</pre>
 *
 * <h2>Why not use {@code OutlineBufferSource} directly?</h2>
 *
 * <p>We initially tried wrapping {@code OutlineBufferSource} and relying on its
 * internal outline generation. However, {@code OutlineBufferSource.getBuffer()}
 * writes to <b>two</b> consumers via {@code VertexMultiConsumer}: a
 * {@code BufferSource} (noop parent) for the textured pass, and an
 * {@code OutlineBufferSource.EntityOutlineGenerator} for the outline pass.</p>
 *
 * <p>The problem: the noop parent {@code BufferSource} is a <i>real</i>
 * {@code BufferSource} that accumulates textured vertex data. When render types
 * switch during entity rendering (e.g., from body to armor layer), the
 * {@code BufferSource} <b>auto-flushes</b> the previous batch to the main
 * framebuffer (because it cannot consolidate consecutive geometry of different
 * types). This caused textured (non-silhouette) entity parts to appear on screen.</p>
 *
 * <p>The fix: bypass {@code OutlineBufferSource} entirely. We implement our own
 * {@link #getBuffer(RenderType)} logic that only writes to the outline buffer,
 * with no secondary textured buffer that could leak to the screen.</p>
 *
 * <h2>Pitfalls encountered during development</h2>
 *
 * <p><b>1. {@code RenderTarget.clear()} breaks subsequent rendering.</b>
 * {@code RenderTarget.clear()} calls {@code bindWrite(true)} followed by
 * {@code unbindWrite()}. But {@code unbindWrite()} binds GL framebuffer 0
 * (the default framebuffer), <i>not</i> MC's {@code mainRenderTarget}. All
 * subsequent rendering goes nowhere. Fix: manually call
 * {@code entityTarget.bindWrite(true)} + {@code GlStateManager._clear()} +
 * {@code mainTarget.bindWrite(false)} instead of {@code entityTarget.clear()}.</p>
 *
 * <p><b>2. {@code RenderSystem.disableBlend()} breaks GUI rendering.</b>
 * After blitting, we initially called {@code disableBlend()}, but GUI elements
 * rendered after the silhouette require blending (for transparency, text, etc.).
 * Fix: use {@code RenderSystem.defaultBlendFunc()} to restore the default blend
 * function without disabling blending.</p>
 *
 * <p><b>3. Auto-flush data loss.</b>
 * {@code BufferSource.getBuffer()} auto-flushes the previous batch when the
 * shared render type changes (see {@code BufferSource} line ~56: when
 * {@code lastSharedType != null}, it calls {@code endBatch(lastSharedType)}).
 * For outline render types, this auto-flush draws data into {@code entityTarget}.
 * If we cleared {@code entityTarget} in {@link #end()} <i>before</i> calling
 * {@code endBatch()}, the auto-flushed data would be lost — only the last batch
 * (still in the buffer at endBatch time) would survive. This caused some entities'
 * body parts to disappear (e.g., mooshroom body, tropical fish body, llama body)
 * because their body data was auto-flushed to {@code entityTarget} when the render
 * type switched to a different layer, then erased by the clear in {@code end()}.
 * Fix: clear {@code entityTarget} in the <b>constructor</b> (before any rendering
 * starts), not in {@code end()}.</p>
 *
 * <p><b>4. Render types without outline variants are silently discarded.</b>
 * Some render types ({@code entityNoOutline}, {@code eyes()}, {@code leash()},
 * {@code entityShadow}, {@code armorEntityGlint}, etc.) have
 * {@code OutlineProperty.NONE} and produce no outline variant. For these, we
 * return a {@link #NOOP} consumer. This means entity shadows, leash lines,
 * enchantment glints, and certain eye layers will not appear in the silhouette.
 * This is intentional — these are decorative/overlay elements, not part of the
 * entity's body shape.</p>
 */
@Environment(EnvType.CLIENT)
public final class SilhouetteMultiBufferSource implements MultiBufferSource {
    /** Discards all vertex data. Used for render types without outline variants. */
    private static final VertexConsumer NOOP = new VertexConsumer() {
        @Override public VertexConsumer addVertex(float x, float y, float z) { return this; }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        @Override public VertexConsumer setUv(float u, float v) { return this; }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
    };

    /**
     * Shared buffer source reused across all silhouette renders.
     * Retains the underlying {@link BufferBuilder}'s allocated memory so it doesn't
     * need to be reallocated every frame. See class-level Javadoc for usage.
     */
    private static final MultiBufferSource.BufferSource outlineBuffer = MultiBufferSource.immediate(new ByteBufferBuilder(1536));

    /** ARGB32 silhouette color, normalized via {@link FastColor.ARGB32#color(int, int, int, int)}. */
    private final int color;

    /**
     * Creates a new silhouette buffer source.
     *
     * <p>Also clears {@code entityTarget} immediately, before any entity rendering
     * begins. See class-level Javadoc pitfall #3 for why this must happen here
     * rather than in {@link #end()}.</p>
     *
     * @param color ARGB32 silhouette color
     */
    public SilhouetteMultiBufferSource(int color) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        this.color = FastColor.ARGB32.color(a, r, g, b);

        // Clear entityTarget BEFORE rendering starts. See pitfall #3 in class Javadoc.
        // We avoid entityTarget.clear() — see pitfall #1 in class Javadoc.
        RenderTarget entityTarget = Minecraft.getInstance().levelRenderer.entityTarget();
        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
        if (entityTarget != null) {
            entityTarget.bindWrite(true);
            GlStateManager._clear(16384 | 256, Minecraft.ON_OSX);
            mainTarget.bindWrite(false);
        }
    }

    /**
     * Returns a vertex consumer that will render the entity part as a silhouette.
     *
     * <p>For render types with an outline variant ({@link RenderType#outline()} returns
     * non-empty), returns a {@link ColorOverrideConsumer} wrapping the outline buffer's
     * consumer. The outline render type's output state ({@code OUTLINE_TARGET}) ensures
     * all writes go to {@code entityTarget}.</p>
     *
     * <p>For render types without an outline variant, returns {@link #NOOP}, silently
     * discarding the vertex data. See pitfall #4 in class Javadoc.</p>
     */
    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        Optional<RenderType> outlineOpt = renderType.outline();
        if (outlineOpt.isPresent()) {
            VertexConsumer delegate = outlineBuffer.getBuffer(outlineOpt.get());
            return new ColorOverrideConsumer(delegate, color);
        }
        return NOOP;
    }

    /**
     * Flushes remaining outline data into {@code entityTarget} and blits it onto the
     * main framebuffer. Must be called after entity rendering is complete.
     *
     * <p>After this call, {@code entityTarget} is cleared again to prevent stale
     * silhouette data from leaking into subsequent world rendering (where
     * {@code entityTarget} is used for the vanilla entity glow effect).</p>
     */
    public void end() {
        RenderTarget entityTarget = Minecraft.getInstance().levelRenderer.entityTarget();
        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
        if (entityTarget == null) return;

        // Flush any remaining (not-yet-auto-flushed) outline data into entityTarget.
        // Data that was already auto-flushed during rendering is already there.
        outlineBuffer.endBatch();

        // Blit entityTarget → main framebuffer with alpha blending.
        // Uses the same blend mode as vanilla LevelRenderer.doEntityOutline().
        // Note: blitToScreen(w, h, false) does NOT disable blend internally.
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO,
                GlStateManager.DestFactor.ONE
        );
        entityTarget.blitToScreen(Minecraft.getInstance().getWindow().getWidth(),
                Minecraft.getInstance().getWindow().getHeight(), false);

        // Restore default blend. Do NOT call disableBlend() — see pitfall #2.
        RenderSystem.defaultBlendFunc();

        // Clear entityTarget to prevent stale data in subsequent world rendering.
        // We avoid entityTarget.clear() — see pitfall #1.
        entityTarget.bindWrite(true);
        GlStateManager._clear(16384 | 256, Minecraft.ON_OSX);
        mainTarget.bindWrite(false);
    }

    /**
     * Mirrors the behavior of vanilla {@code OutlineBufferSource.EntityOutlineGenerator}:
     * overrides vertex color to a fixed value, delegates UV coordinates (needed for
     * texture alpha masking by the {@code rendertype_outline} shader), and no-ops
     * everything else (overlay, lightmap, normal).
     *
     * <p>The {@code rendertype_outline} shader uses vertex color for RGB output and
     * texture alpha for masking, producing a solid color where the texture is opaque
     * and transparency where the texture is transparent.</p>
     */
    private static class ColorOverrideConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final int color;

        ColorOverrideConsumer(VertexConsumer delegate, int color) {
            this.delegate = delegate;
            this.color = color;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z).setColor(color);
            return this;
        }

        /** No-op: the silhouette color is set in {@link #addVertex}, not here. */
        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return this;
        }

        /**
         * Delegate UV to the outline consumer. The {@code rendertype_outline} shader
         * uses UV to sample the texture for alpha masking — transparent texture areas
         * become transparent in the silhouette.
         */
        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        /** No-op: overlay coordinates are not used by the outline shader. */
        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        /** No-op: lightmap coordinates are not used by the outline shader. */
        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        /** No-op: normals are not used by the outline shader. */
        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return this;
        }
    }
}
