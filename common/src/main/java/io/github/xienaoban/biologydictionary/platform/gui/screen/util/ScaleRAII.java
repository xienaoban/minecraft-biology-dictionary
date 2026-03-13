package io.github.xienaoban.biologydictionary.platform.gui.screen.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;

@Environment(EnvType.CLIENT)
public final class ScaleRAII implements AutoCloseable {
    private final GuiGraphics guiGraphics;

    ScaleRAII(ScreenRenderingContext ctx, float size) {
        guiGraphics = ctx.getGuiGraphics();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(size, size, size);
    }

    // TODO: remove it
    ScaleRAII(ScreenRenderingContext ctx, float size, float z) {
        this(ctx, size);
        guiGraphics.pose().translate(0, 0, z);
    }

    @Override
    public void close() {
        guiGraphics.pose().popPose();
    }
}
