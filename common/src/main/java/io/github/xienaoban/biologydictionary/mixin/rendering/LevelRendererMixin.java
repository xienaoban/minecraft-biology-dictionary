package io.github.xienaoban.biologydictionary.mixin.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.biologydictionary.client.HighlightRenderer;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ClientOnly
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(method = "submitEntities(Lcom/mojang/blaze3d/vertex/PoseStack;"
            + "Lnet/minecraft/client/renderer/state/level/LevelRenderState;"
            + "Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
            at = @At(value = "TAIL"))
    private void biologydictionary$submitHighlight(PoseStack poseStack, LevelRenderState levelRenderState,
                                                   SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
        ClientWorldSession cws = ClientWorldSession.get();
        if (cws != null && cws.getHighlightManager().hasAnyHighlighted()) {
            levelRenderState.shouldShowEntityOutlines = true;
        }
        HighlightRenderer.submit(Minecraft.getInstance(),
                ((LevelRenderer) (Object) this).entityRenderDispatcher(),
                poseStack, levelRenderState, submitNodeCollector);
    }
}
