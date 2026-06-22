package io.github.xienaoban.biologydictionary.mixin.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.Minecraft;
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

@ClientOnly
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(
            method = "renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                    + "Lnet/minecraft/client/player/LocalPlayer;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderAllFeatures()V",
                    shift = At.Shift.AFTER))
    private void biologydictionary$renderFirstPerson(float tickDelta, PoseStack poseStack,
                                                    SubmitNodeCollector submitNodeCollector, LocalPlayer localPlayer,
                                                    int light, CallbackInfo ci) {
        ClientWorldSession cws = ClientWorldSession.get();
        if (cws == null) { return; }
        cws.getShoulderEntityRenderer().run(
                minecraft, entityRenderDispatcher, tickDelta, poseStack, submitNodeCollector, localPlayer, light);
    }
}
