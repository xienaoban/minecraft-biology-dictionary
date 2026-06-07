package io.github.xienaoban.biologydictionary.platform.gui.screen.util;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.gui.GuiGraphics;

@ClientOnly
public final class ScaleRAII implements AutoCloseable {
    private final GuiGraphics guiGraphics;

    ScaleRAII(ScreenRenderingContext ctx, float size) {
        guiGraphics = ctx.getGuiGraphics();
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(size, size);
    }

    // TODO: remove it
    ScaleRAII(ScreenRenderingContext ctx, float size, float z) {
        this(ctx, size);
        guiGraphics.pose().translate(0, 0);
    }

    @Override
    public void close() {
        guiGraphics.pose().popMatrix();
    }
}
