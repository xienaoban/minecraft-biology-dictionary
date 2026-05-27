package io.github.xienaoban.biologydictionary.mixin.rendering;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@ClientOnly
@Mixin(GuiGraphics.class)
public interface GuiGraphicsIMixin {
    @Accessor("scissorStack")
    GuiGraphics.ScissorStack biologydictionary$getScissorStack();

    @Accessor("guiRenderState")
    GuiRenderState biologydictionary$getGuiRenderState();
}
