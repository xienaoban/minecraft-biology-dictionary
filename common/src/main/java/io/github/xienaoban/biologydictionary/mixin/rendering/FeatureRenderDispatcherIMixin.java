package io.github.xienaoban.biologydictionary.mixin.rendering;

import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FeatureRenderDispatcher.class)
public interface FeatureRenderDispatcherIMixin {
    @Accessor("outlineBufferSource")
    OutlineBufferSource biologydictionary$getOutlineBufferSource();
}
