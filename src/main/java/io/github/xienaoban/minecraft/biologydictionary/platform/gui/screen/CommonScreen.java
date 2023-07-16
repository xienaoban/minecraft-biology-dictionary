package io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen;

import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.platform.mixin.GuiGraphicsIMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

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

    protected void beforeRender(ScreenRenderingContext ctx) {}

    protected void render(ScreenRenderingContext ctx) {}

    protected void afterRender(ScreenRenderingContext ctx) {
        super.render(ctx.getGuiGraphics(), (int) ctx.getMouseX(), (int) ctx.getMouseY(), ctx.getTickDelta());
    }

    public final int getZ() { return 0; }

    public final void renderText(ScreenRenderingContext ctx, Component component, int color, int x, int y) {
        ctx.getGuiGraphics().drawString(font, component, x, y, color, false);
    }

    public final void renderCenteredText(ScreenRenderingContext ctx, Component component, int color, int x, int y) {
        ctx.getGuiGraphics().drawCenteredString(font, component, x, y, color);
    }

    public final void renderHorizontalLine(ScreenRenderingContext ctx, int color, int width, int z, int y, int left, int right) {
        renderRectangle(ctx, color, z, left, y, right, y + width);
    }

    public final void renderVerticalLine(ScreenRenderingContext ctx, int color, int width, int z, int x, int top, int bottom) {
        renderRectangle(ctx, color, z, x, top, x + width, bottom);
    }

    public final void renderRectangle(ScreenRenderingContext ctx, int color, int width, int z, int left, int top, int right, int bottom) {
        renderHorizontalLine(ctx, color, width, z, top, left, right);
        renderHorizontalLine(ctx, color, width, z, bottom - width, left, right);
        renderVerticalLine(ctx, color, width, z, left, top, bottom);
        renderVerticalLine(ctx, color, width, z, right - width, top, bottom);
    }

    /**
     * @see net.minecraft.client.gui.GuiGraphics#fill(RenderType, int, int, int, int, int, int)
     */
    public final void renderRectangle(ScreenRenderingContext ctx, int color, float z, float left, float top, float right, float bottom) {
        ctx.getGuiGraphics().fill((int) left, (int) top, (int) right, (int) bottom, (int) z, color);
    }

    public final void renderTexture(ScreenRenderingContext ctx, ResourceLocation texture,
                                    int resourceWidth, int resourceHeight,
                                    int textureLeft, int textureTop,
                                    int z, int left, int top, int width, int height) {
        renderTexture(ctx, texture, resourceWidth, resourceHeight,
                textureLeft, textureTop, textureLeft + width, textureTop + height,
                z, left, top, left + width,  top + height);
    }

    /**
     * @see net.minecraft.client.gui.GuiGraphics#innerBlit(ResourceLocation, int, int, int, int, int, float, float, float, float)
     */
    public final void renderTexture(ScreenRenderingContext ctx, ResourceLocation texture,
                                    int resourceWidth, int resourceHeight,
                                    int textureLeft, int textureTop, int textureRight, int textureBottom,
                                    int z, int left, int top, int right, int bottom) {
        ((GuiGraphicsIMixin) ctx.getGuiGraphics()).callInnerBlit(texture, left, right, top, bottom, z,
                (float) textureLeft / (float) resourceWidth, (float) textureRight / (float) resourceWidth,
                (float) textureTop / (float) resourceHeight, (float) textureBottom / (float) resourceHeight);
    }

    /**
     * @see net.minecraft.client.gui.screens.inventory.PageButton#playDownSound(SoundManager)
     */
    public static void playScreenSound(SoundEvent sound, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }
}
