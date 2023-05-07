package io.github.xienaoban.minecraft.biologydictionary.platform.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.minecraft.biologydictionary.platform.util.ListenerList;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

public final class RenderingRegistry {
    public static final ListenerList<RenderingListener> FIRST_PERSON_RENDERING_EVENT = new ListenerList<>(RenderingListener.class);

    public static void registerFirstPersonRendering(RenderingListener listener) {
        FIRST_PERSON_RENDERING_EVENT.addListener(listener);
    }

    @FunctionalInterface
    public interface RenderingListener {
        void run(EntityRenderDispatcher entityRenderDispatcher, float tickDelta, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer player, int light);
    }
}
