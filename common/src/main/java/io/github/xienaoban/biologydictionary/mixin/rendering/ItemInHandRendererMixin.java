package io.github.xienaoban.biologydictionary.mixin.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to {@link ItemInHandRenderer} to render first-person shoulder entities.
 *
 * @see net.minecraft.client.renderer.ItemInHandRenderer#renderHandsWithItems(float, PoseStack, MultiBufferSource.BufferSource, LocalPlayer, int)
 */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    /**
     * Injects before {@code bufferSource.endBatch()} to render entities on player's shoulders.
     *
     * <p>This injection point ensures that shoulder entities (like parrots) are rendered
     * after all hand items but before the batch is finalized.</p>
     *
     * @param f Partial tick time
     * @param poseStack The pose stack
     * @param bufferSource The buffer source
     * @param localPlayer The local player
     * @param i Packed light coordinates
     * @param ci Callback info
     */
    @Inject(
            method = "renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/player/LocalPlayer;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V")
    )
    private void biologydictionary$renderFirstPerson(float f, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer localPlayer, int i, CallbackInfo ci) {
        ClientWorldSession session = ClientWorldSession.get();
        if (session != null) {
            session.getShoulderEntityRenderer().run(entityRenderDispatcher, f, poseStack, bufferSource, localPlayer, i);
        }
    }
}
