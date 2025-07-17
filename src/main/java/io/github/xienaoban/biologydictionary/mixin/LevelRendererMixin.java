package io.github.xienaoban.biologydictionary.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.biologydictionary.client.HighlightRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow
    @Final private Minecraft minecraft;

    @Shadow
    @Final private RenderBuffers renderBuffers;

    @Shadow
    private ClientLevel level;

    @Inject(method = "renderEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/Camera;Lnet/minecraft/client/DeltaTracker;Ljava/util/List;)V",
            at = @At(value = "TAIL"))
    private void renderHighlight(PoseStack poseStack, MultiBufferSource.BufferSource ignored, Camera camera, DeltaTracker deltaTracker, List<Entity> list, CallbackInfo ci) {
        OutlineBufferSource bufferSource = renderBuffers.outlineBufferSource();
        HighlightRenderer.render(minecraft, level, poseStack, bufferSource, camera, deltaTracker);
    }
}
