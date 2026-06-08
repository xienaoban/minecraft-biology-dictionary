package io.github.xienaoban.biologydictionary.mixin.rendering;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@ClientOnly
@Mixin(GuiGraphics.class)
public interface GuiGraphicsIMixin {
    @Accessor("scissorStack")
    GuiGraphics.ScissorStack biologydictionary$getScissorStack();

    @Invoker("flushIfUnmanaged")
    void biologydictionary$invokeFlushIfUnmanaged();
}
