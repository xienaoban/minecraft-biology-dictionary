package io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util;

import net.minecraft.client.gui.GuiGraphics;

public final class ScaleRAII implements AutoCloseable {
    private final GuiGraphics guiGraphics;

    ScaleRAII(ScreenRenderingContext ctx, float size) {
        guiGraphics = ctx.getGuiGraphics();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(size, size, size);
    }

    @Override
    public void close() {
        guiGraphics.pose().popPose();
    }
}
