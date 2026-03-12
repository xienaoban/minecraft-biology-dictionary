package io.github.xienaoban.biologydictionary.mixin.rendering;

import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsIMixin {
    @Accessor("scissorStack")
    GuiGraphics.ScissorStack biologydictionary$getScissorStack();
}
