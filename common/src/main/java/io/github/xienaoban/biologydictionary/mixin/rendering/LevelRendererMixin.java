package io.github.xienaoban.biologydictionary.mixin.rendering;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.xienaoban.biologydictionary.client.HighlightManager;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Unique private boolean biologydictionary$shouldHighlightEntity;

    @Inject(method = "shouldShowEntityOutlines", at = @At("HEAD"), cancellable = true)
    private void biologydictionary$injectShouldShowEntityOutlines(CallbackInfoReturnable<Boolean> cir) {
        ClientWorldSession cws = ClientWorldSession.get();
        if (cws != null && cws.getHighlightManager().hasAnyHighlighted()) {
            cir.setReturnValue(true);
        }
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"))
    private Iterable<Entity> biologydictionary$modifyEntitiesForRendering(Iterable<Entity> original) {
        ClientWorldSession cws = ClientWorldSession.get();
        if (cws != null) {
            HighlightManager hm = cws.getHighlightManager();
            if (hm.hasAnyHighlighted() && !hm.getHighlightedBlocks().isEmpty()) {
                Stream<Entity> entitiesStream = StreamSupport.stream(original.spliterator(), false);
                Stream<Entity> blocksStream = hm.getHighlightedBlocks().stream()
                        .map(HighlightManager.HighlightedBlock::getFallingBlockEntity);
                return Stream.concat(entitiesStream, blocksStream)::iterator;
            }
        }
        return original;
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z"))
    private boolean biologydictionary$modifyShouldRender(boolean original, @Local Entity entity) {
        ClientWorldSession cws = ClientWorldSession.get();
        if (cws != null && cws.getHighlightManager().isEntityHighlighted(entity)) {
            biologydictionary$shouldHighlightEntity = true;
            return true;
        }
        biologydictionary$shouldHighlightEntity = false;
        return original;
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;isOutsideBuildHeight(I)Z"))
    private boolean biologydictionary$modifyIsOutsideBuildHeight(boolean original) {
        if (biologydictionary$shouldHighlightEntity) {
            return true;
        }
        return original;
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean biologydictionary$modifyShouldEntityAppearGlowing(boolean original) {
        if (biologydictionary$shouldHighlightEntity) {
            return true;
        }
        return original;
    }
}
