package io.github.xienaoban.biologydictionary.mixin.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ClientOnly
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class FirstPersonHandsAndItemsRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(
            method = "submitHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                    + "Lnet/minecraft/client/renderer/state/level/PlayerRenderState;"
                    + "Lnet/minecraft/client/renderer/state/level/FirstPersonHandsAndItemsRenderState;)V",
            at = @At("TAIL"))
    private void biologydictionary$renderFirstPerson(float tickDelta, PoseStack poseStack,
                                                     SubmitNodeCollector submitNodeCollector,
                                                     PlayerRenderState playerRenderState,
                                                     FirstPersonHandsAndItemsRenderState handsItemsRenderState,
                                                     CallbackInfo ci) {
        if (playerRenderState.avatarRenderState == null) { return; }
        LocalPlayer localPlayer = minecraft.player;
        if (localPlayer == null) { return; }
        ClientWorldSession cws = ClientWorldSession.get();
        if (cws == null) { return; }
        cws.getShoulderEntityRenderer().run(
                minecraft, minecraft.getEntityRenderDispatcher(), tickDelta, poseStack, submitNodeCollector,
                localPlayer, playerRenderState.avatarRenderState.lightCoords);
    }
}
