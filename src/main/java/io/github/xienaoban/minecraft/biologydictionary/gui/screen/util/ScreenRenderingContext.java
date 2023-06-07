package io.github.xienaoban.minecraft.biologydictionary.gui.screen.util;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.CommonScreen;

public final class ScreenRenderingContext {
    private final CommonScreen screen;
    private PoseStack poseStack;
    private int mouseX, mouseY;
    private float tickDelta;
    private boolean renderBox;

    public ScreenRenderingContext(CommonScreen screen) {
        this.screen = screen;
    }

    public CommonScreen getScreen() { return screen; }
    public PoseStack getPoseStack() { return poseStack; }
    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }
    public float getTickDelta() { return tickDelta; }
    public boolean shouldRenderBox() { return renderBox; }

    public void update(PoseStack poseStack, int mouseX, int mouseY, float tickDelta) {
        this.poseStack = poseStack;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.tickDelta = tickDelta;
    }

    public void setRenderBox(boolean renderBox) {
        this.renderBox = renderBox;
    }
}
