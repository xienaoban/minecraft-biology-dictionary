package io.github.xienaoban.minecraft.biologydictionary.platform.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsIMixin {
    @Invoker
    void callFlushIfUnmanaged();

    @Invoker
    void callFlushIfManaged();

    @Invoker
    void callInnerBlit(ResourceLocation resourceLocation, int i, int j, int k, int l, int m, float f, float g, float h, float n);
}
