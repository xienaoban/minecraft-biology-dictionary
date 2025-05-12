package io.github.xienaoban.minecraft.biologydictionary.common.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsIMixin {
    @Accessor
    MultiBufferSource.BufferSource getBufferSource();
}
