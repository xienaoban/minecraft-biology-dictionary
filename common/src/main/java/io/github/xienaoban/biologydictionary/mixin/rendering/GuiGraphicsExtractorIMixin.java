package io.github.xienaoban.biologydictionary.mixin.rendering;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsExtractorIMixin {
    @Accessor("guiRenderState")
    GuiRenderState biologydictionary$getGuiRenderState();

    @Accessor("scissorStack")
    GuiGraphicsExtractor.ScissorStack biologydictionary$getScissorStack();
}
