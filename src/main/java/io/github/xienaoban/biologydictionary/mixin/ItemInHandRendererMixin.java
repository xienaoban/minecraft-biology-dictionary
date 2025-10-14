package io.github.xienaoban.biologydictionary.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.biologydictionary.client.FirstPersonShoulderEntityRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderAllFeatures()V", shift = At.Shift.AFTER))
    private void renderFirstPerson(float f, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LocalPlayer localPlayer, int i, CallbackInfo ci) {
        FirstPersonShoulderEntityRenderer.run(entityRenderDispatcher, f, poseStack, submitNodeCollector, localPlayer, i);
    }
}
