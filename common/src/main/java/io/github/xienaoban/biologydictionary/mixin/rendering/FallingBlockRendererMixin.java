package io.github.xienaoban.biologydictionary.mixin.rendering;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.xienaoban.biologydictionary.client.HighlightManager;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FallingBlockRenderer.class)
public class FallingBlockRendererMixin {

    /**
     * To make {@code blockState != level.getBlockState(fallingBlockEntity.blockPosition())} be true.
     */
    @ModifyExpressionValue(method = "render(Lnet/minecraft/world/entity/item/FallingBlockEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState biologydictionary$modifyGetBlockState(BlockState original, FallingBlockEntity fallingBlockEntity) {
        if (fallingBlockEntity instanceof HighlightManager.ClientHighlightedBlockEntity) { return null; }
        return original;
    }
}
