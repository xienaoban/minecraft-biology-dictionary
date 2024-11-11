package io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen;

import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Why wrap the rendering APIs here?
 * 1. Because the APIs always change between different MC versions.
 * 2. Because the parameter names of the methods are missing when using the official mappings.
 */
@Environment(EnvType.CLIENT)
public abstract class CommonScreen extends Screen {
    protected final ScreenRenderingContext screenRenderingContext;

    protected CommonScreen(Component component) {
        super(component);
        this.screenRenderingContext = new ScreenRenderingContext(this);
    }

    @Override
    public final void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
        screenRenderingContext.update(guiGraphics, mouseX, mouseY, tickDelta);
        beforeRender(screenRenderingContext);
        render(screenRenderingContext);
        afterRender(screenRenderingContext);
    }

    public final void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {}

    public final void renderBlurredBackground(ScreenRenderingContext ctx) {
        super.renderBlurredBackground();
    }

    public final void renderTransparentBackground(ScreenRenderingContext ctx) {
        super.renderTransparentBackground(ctx.getGuiGraphics());
    }

    protected void beforeRender(ScreenRenderingContext ctx) {}

    protected void render(ScreenRenderingContext ctx) {}

    protected void afterRender(ScreenRenderingContext ctx) {
        super.render(ctx.getGuiGraphics(), (int) ctx.getMouseX(), (int) ctx.getMouseY(), ctx.getTickDelta());
    }

    public Font getFont() { return font; }

    public float getZ() { return 0; }
}
