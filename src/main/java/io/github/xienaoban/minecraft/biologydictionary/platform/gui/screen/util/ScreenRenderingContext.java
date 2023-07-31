package io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util;

import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.CommonScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

@Environment(EnvType.CLIENT)
public final class ScreenRenderingContext {
    private final CommonScreen screen;

    Minecraft minecraft;
    GuiGraphics guiGraphics;
    private float mouseX, mouseY;
    private float tickDelta;
    private boolean debug;

    public ScreenRenderingContext(CommonScreen screen) {
        this.minecraft = Minecraft.getInstance();
        this.screen = screen;
        this.debug = true;
    }

    public CommonScreen getScreen() { return screen; }
    public GuiGraphics getGuiGraphics() { return guiGraphics; }
    public float getMouseX() { return mouseX; }
    public float getMouseY() { return mouseY; }
    public float getTickDelta() { return tickDelta; }
    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }

    /**
     * We don't use the mouseX and mouseY parameters because they are int.
     * @see net.minecraft.client.renderer.GameRenderer#render(float, long, boolean)
     *
     * @param mouseX not used
     * @param mouseY not used
     */
    public void update(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
        this.guiGraphics = guiGraphics;
        this.tickDelta = tickDelta;

        this.mouseX = (float) minecraft.mouseHandler.xpos() * (float) minecraft.getWindow().getGuiScaledWidth() / (float) minecraft.getWindow().getScreenWidth();
        this.mouseY = (float) minecraft.mouseHandler.ypos() * (float) minecraft.getWindow().getGuiScaledHeight() / (float) minecraft.getWindow().getScreenHeight();
        assert mouseX == (int) this.mouseX && mouseY == (int) this.mouseY;
    }
}
