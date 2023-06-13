package io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util;

import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.CommonScreen;
import net.minecraft.client.gui.GuiGraphics;

public final class ScreenRenderingContext {
    private final CommonScreen screen;

    GuiGraphics guiGraphics;
    private int mouseX, mouseY;
    private float tickDelta;
    private boolean debug;

    public ScreenRenderingContext(CommonScreen screen) {
        this.screen = screen;
    }

    public CommonScreen getScreen() { return screen; }
    public GuiGraphics getGuiGraphics() { return guiGraphics; }
    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }
    public float getTickDelta() { return tickDelta; }
    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }

    public void update(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
        this.guiGraphics = guiGraphics;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.tickDelta = tickDelta;
    }
}
