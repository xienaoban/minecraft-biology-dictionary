package io.github.xienaoban.biologydictionary.mixin.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.biologydictionary.compat.CompatibilityOptions;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.mixin.entity.EntityIMixin;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderDispatcher.class, priority = 900)
public class EntityRenderDispatcherMixin {
    @Unique private boolean biologydictionary$modifiedGlowing;

    /**
     * Star Optimized cancels entity rendering when bufferSource is OutlineBufferSource and the entity
     * is not actually glowing (EntityRenderDispatcher#onRender). Our highlight system forces outline
     * rendering via LevelRendererMixin but does not set the real glowing flag, so Star Opt cancels
     * our highlighted entities. Workaround: temporarily set the glowing flag before Star Opt's check.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void biologydictionary$beforeRender(Entity entity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (!CompatibilityOptions.entityOutlineCompatStarOpt()) return;
        biologydictionary$modifiedGlowing = false;
        if (multiBufferSource instanceof OutlineBufferSource) {
            ClientWorldSession cws = ClientWorldSession.get();
            if (cws != null && cws.getHighlightManager().isEntityHighlighted(entity)
                    && !entity.isCurrentlyGlowing()) {
                ((EntityIMixin) entity).biologydictionary$setSharedFlag(EntityIMixin.biologydictionary$getFlagGlowing(), true);
                biologydictionary$modifiedGlowing = true;
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void biologydictionary$afterRender(Entity entity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (!CompatibilityOptions.entityOutlineCompatStarOpt()) return;
        if (biologydictionary$modifiedGlowing) {
            ((EntityIMixin) entity).biologydictionary$setSharedFlag(EntityIMixin.biologydictionary$getFlagGlowing(), false);
            biologydictionary$modifiedGlowing = false;
        }
    }
}
