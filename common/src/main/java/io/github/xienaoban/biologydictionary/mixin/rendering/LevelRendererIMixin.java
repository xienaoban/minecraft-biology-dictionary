package io.github.xienaoban.biologydictionary.mixin.rendering;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@ClientOnly
@Mixin(LevelRenderer.class)
public interface LevelRendererIMixin {
    @Accessor("levelRenderState")
    LevelRenderState biologydictionary$getLevelRenderState();
}
