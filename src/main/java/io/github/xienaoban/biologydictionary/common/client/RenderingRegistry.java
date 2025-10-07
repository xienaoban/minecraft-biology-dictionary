package io.github.xienaoban.biologydictionary.common.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.biologydictionary.common.util.ListenerList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

@Environment(EnvType.CLIENT)
public final class RenderingRegistry {
    public static final ListenerList<RenderingListener> FIRST_PERSON_RENDERING_EVENT = new ListenerList<>(RenderingListener.class);

    public static void registerFirstPersonRendering(RenderingListener listener) {
        FIRST_PERSON_RENDERING_EVENT.addListener(listener);
    }

    @FunctionalInterface
    public interface RenderingListener {
        void run(EntityRenderDispatcher entityRenderDispatcher, float tickDelta, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LocalPlayer player, int light);
    }
}
