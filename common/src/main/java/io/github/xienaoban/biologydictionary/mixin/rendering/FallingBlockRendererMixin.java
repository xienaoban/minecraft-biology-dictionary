package io.github.xienaoban.biologydictionary.mixin.rendering;

import io.github.xienaoban.biologydictionary.client.HighlightManager;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FallingBlockRenderer.class)
public class FallingBlockRendererMixin {

    /**
     * To make {@code blockState != level.getBlockState(fallingBlockEntity.blockPosition())} be true.
     */
    @Redirect(method = "render(Lnet/minecraft/world/entity/item/FallingBlockEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState biologydictionary$redirectGetBlockState(Level instance, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
        if (fallingBlockEntity instanceof HighlightManager.ClientHighlightedBlockEntity) { return null; }
        return instance.getBlockState(blockPos);
    }
}
