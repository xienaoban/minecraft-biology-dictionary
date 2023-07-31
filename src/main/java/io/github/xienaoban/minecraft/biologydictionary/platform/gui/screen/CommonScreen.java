package io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen;

import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.platform.mixin.GuiGraphicsIMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;

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

    public float getZ() { return 0; }

    public final void renderText(ScreenRenderingContext ctx, Component component, int color, float x, float y) {
        ctx.getGuiGraphics().drawString(font, component, (int) x, (int) y, color, false);
    }

    public final void renderCenteredText(ScreenRenderingContext ctx, Component component, int color, float x, float y) {
        ctx.getGuiGraphics().drawString(font, component, (int) x - font.width(component) / 2, (int) y, color, false);
    }

    public final void renderHorizontalLine(ScreenRenderingContext ctx, int color, float width, float z, float y, float left, float right) {
        renderRectangle(ctx, color, z, left, y, right, y + width);
    }

    public final void renderVerticalLine(ScreenRenderingContext ctx, int color, float width, float z, float x, float top, float bottom) {
        renderRectangle(ctx, color, z, x, top, x + width, bottom);
    }

    public final void renderRectangle(ScreenRenderingContext ctx, int color, float width, float z, float left, float top, float right, float bottom) {
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
                                    float resourceWidth, float resourceHeight,
                                    float textureLeft, float textureTop,
                                    float z, float left, float top, float width, float height) {
        renderTexture(ctx, texture, resourceWidth, resourceHeight,
                textureLeft, textureTop, textureLeft + width, textureTop + height,
                z, left, top, left + width,  top + height);
    }

    /**
     * @see net.minecraft.client.gui.GuiGraphics#innerBlit(ResourceLocation, int, int, int, int, int, float, float, float, float)
     */
    public final void renderTexture(ScreenRenderingContext ctx, ResourceLocation texture,
                                    float resourceWidth, float resourceHeight,
                                    float textureLeft, float textureTop, float textureRight, float textureBottom,
                                    float z, float left, float top, float right, float bottom) {
        ((GuiGraphicsIMixin) ctx.getGuiGraphics()).callInnerBlit(texture, (int) left, (int) right, (int) top, (int) bottom, (int) z,
                textureLeft / resourceWidth, textureRight / resourceWidth,
                textureTop / resourceHeight, textureBottom / resourceHeight);
    }

    public static void renderLivingEntityFollowsMouse(ScreenRenderingContext ctx, Entity entity, float midX, float bottom, float scale, float rotateX, float rotateY) {
        InventoryScreen.renderEntityInInventoryFollowsMouse(ctx.getGuiGraphics(), (int) midX, (int) bottom, (int) scale, rotateX, rotateY, (LivingEntity) entity);
    }

    /**
     * @see net.minecraft.client.gui.screens.inventory.InventoryScreen#renderEntityInInventoryFollowsMouse(GuiGraphics, int, int, int, float, float, LivingEntity)
     * @see net.minecraft.client.gui.screens.inventory.InventoryScreen#renderEntityInInventory(GuiGraphics, int, int, int, Quaternionf, Quaternionf, LivingEntity)
     */
    public static void renderEntity(ScreenRenderingContext ctx, Entity entity, float midX, float bottom, float scale, float rotateX, float rotateY) {
    }

    /**
     * @see net.minecraft.client.gui.screens.inventory.PageButton#playDownSound(SoundManager)
     */
    public static void playScreenSound(SoundEvent sound, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }
}
