package io.github.xienaoban.biologydictionary.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.util.ARGB;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Builds and holds the fog UBO used to render undiscovered entities as flat
 * silhouettes inside the GUI picture-in-picture pass.
 *
 * <p>Background: in 26.2 the entity outline pipeline writes to the world renderer's
 * {@code OUTLINE_TARGET} (see {@code OutputTarget.OUTLINE_TARGET} -> {@code LevelRenderer#entityOutlineTarget}),
 * which the GUI picture-in-picture pass never reads — so {@code renderState.outlineColor}
 * is silently ignored in the GUI and the colored body shows through. Reusing the vanilla
 * fog path fixes this cleanly: entity shaders already bind the FOG bind group, and
 * {@code apply_fog} ({@code fog.glsl}) emits {@code vec4(fogColor.rgb, inColor.a)} —
 * a solid colored silhouette whose shape still comes from the texture alpha.</p>
 *
 * <p>The UBO is filled so that {@code fogValue} is always 1 (full fog): making
 * {@code environmentalStart == environmentalEnd <= 0} forces {@code linear_fog_value}
 * to return 1 for every positive vertex distance, and {@code fogColor.a = 1} is required
 * because {@code apply_fog} multiplies the blend factor by it (alpha &lt; 1 under-blends
 * and the body color leaks through). Therefore the silhouette is opaque even though
 * {@link Colors#UNDISCOVERED_ENTITY_COLOR} carries alpha {@code 0xCC} — we take only its
 * RGB and force the fog alpha to 1.</p>
 */
@ClientOnly
public final class SilhouetteFogBuffer {
    /** start == end <= 0 makes linear_fog_value return 1 for every vertexDistance > 0. */
    private static final float FULL_FOG_EDGE = -1.0F;
    /** Disable the render-distance fog term so only the environmental term decides. */
    private static final float FOG_OFF = Float.MAX_VALUE;

    private static GpuBufferSlice silhouetteSlice;
    private static boolean active;
    private static GpuBufferSlice prevFog;

    private SilhouetteFogBuffer() {
    }

    private static GpuBufferSlice getSilhouetteSlice() {
        if (silhouetteSlice == null) {
            silhouetteSlice = build();
        }
        return silhouetteSlice;
    }

    private static GpuBufferSlice build() {
        int color = Colors.UNDISCOVERED_ENTITY_COLOR; // 0xCCCAB58C — RGB reused, alpha forced below
        Vector4f fogColor = new Vector4f(
                ARGB.redFloat(color),
                ARGB.greenFloat(color),
                ARGB.blueFloat(color),
                1.0F); // fog strength: must be 1.0 or apply_fog under-blends and the body leaks

        ByteBuffer buffer = ByteBuffer.allocateDirect(FogRenderer.FOG_UBO_SIZE).order(ByteOrder.nativeOrder());
        Std140Builder.intoBuffer(buffer)
                .putVec4(fogColor)
                .putFloat(FULL_FOG_EDGE) // environmentalStart
                .putFloat(FULL_FOG_EDGE) // environmentalEnd
                .putFloat(FOG_OFF)       // renderDistanceStart
                .putFloat(FOG_OFF)       // renderDistanceEnd
                .putFloat(FOG_OFF)       // skyEnd
                .putFloat(FOG_OFF);      // cloudEnd
        buffer.flip();

        // usage 128 mirrors FogRenderer's emptyBuffer (uniform, write-once, read-only).
        GpuBuffer gpuBuffer = RenderSystem.getDevice()
                .createBuffer(() -> "BiologyDictionary silhouette fog", 128, buffer);
        return gpuBuffer.slice(0L, FogRenderer.FOG_UBO_SIZE);
    }

    /**
     * Swap the current shader fog for the silhouette fog. Call at the HEAD of the
     * picture-in-picture {@code prepare} call — which spans both the submit
     * ({@code renderToTexture}) and the actual draw ({@code renderAllFeatures}) — so the
     * fog is live when entity geometry is drawn. Render-thread only; not reentrant.
     */
    public static void beginSilhouette() {
        if (active) {
            return;
        }
        prevFog = RenderSystem.getShaderFog();
        RenderSystem.setShaderFog(getSilhouetteSlice());
        active = true;
    }

    /**
     * Restore the fog saved by {@link #beginSilhouette()}. No-op when inactive, so it can
     * be called unconditionally at the TAIL of {@code prepare}.
     */
    public static void endSilhouette() {
        if (!active) {
            return;
        }
        RenderSystem.setShaderFog(prevFog);
        prevFog = null;
        active = false;
    }
}
