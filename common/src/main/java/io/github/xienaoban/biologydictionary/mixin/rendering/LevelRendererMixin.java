package io.github.xienaoban.biologydictionary.mixin.rendering;

import io.github.xienaoban.biologydictionary.client.HighlightManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Unique private boolean biologydictionary$shouldHighlightEntity;

    @Inject(method = "shouldShowEntityOutlines", at = @At("HEAD"), cancellable = true)
    private void biologydictionary$injectShouldShowEntityOutlines(CallbackInfoReturnable<Boolean> cir) {
        if (HighlightManager.hasAnyHighlighted()) {
            cir.setReturnValue(true);
        }
    }

    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z"))
    private <E extends Entity> boolean biologydictionary$redirectShouldRender(EntityRenderDispatcher instance, E entity, Frustum frustum, double d, double e, double f) {
        if (HighlightManager.isEntityHighlighted(entity)) {
            biologydictionary$shouldHighlightEntity = true;
            return true;
        }
        biologydictionary$shouldHighlightEntity = false;
        return instance.shouldRender(entity, frustum, d, e, f);
    }

    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;isOutsideBuildHeight(I)Z"))
    private boolean biologydictionary$redirectIsOutsideBuildHeight(ClientLevel instance, int i) {
        if (biologydictionary$shouldHighlightEntity) {
            return true;
        }
        return instance.isOutsideBuildHeight(i);
    }

    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean biologydictionary$redirectShouldEntityAppearGlowing(Minecraft instance, Entity entity) {
        if (biologydictionary$shouldHighlightEntity) {
            return true;
        }
        return instance.shouldEntityAppearGlowing(entity);
    }
}
