package io.github.xienaoban.biologydictionary.platform.gui.screen.util;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;

@ClientOnly
public final class ScaleRAII implements AutoCloseable {
    static final ScaleRAII DO_NOTHING = new ScaleRAII();

    private final PoseStack poseStack;

    ScaleRAII(ScreenRenderingContext ctx, float size) {
        poseStack = ctx.getGuiGraphics().pose();
        poseStack.pushPose();
        poseStack.scale(size, size, size);
    }

    ScaleRAII(ScreenRenderingContext ctx, float size, float z) {
        this(ctx, size);
        poseStack.translate(0, 0, z);
    }

    private ScaleRAII() {
        poseStack = null;
    }

    @Override
    public void close() {
        if (poseStack != null) {
            poseStack.popPose();
        }
    }
}
