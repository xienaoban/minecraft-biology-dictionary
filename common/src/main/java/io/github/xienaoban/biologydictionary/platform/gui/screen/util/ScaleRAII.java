package io.github.xienaoban.biologydictionary.platform.gui.screen.util;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@ClientOnly
public final class ScaleRAII implements AutoCloseable {
    private final GuiGraphicsExtractor guiGraphics;

    ScaleRAII(ScreenRenderingContext ctx, float size) {
        guiGraphics = ctx.getGuiGraphics();
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(size, size);
    }

    ScaleRAII(ScreenRenderingContext ctx, float size, float z) {
        this(ctx, size);
    }

    @Override
    public void close() {
        guiGraphics.pose().popMatrix();
    }
}
