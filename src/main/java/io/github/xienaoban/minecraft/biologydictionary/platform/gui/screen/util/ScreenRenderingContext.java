package io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util;

import com.mojang.blaze3d.platform.Lighting;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.CommonScreen;
import io.github.xienaoban.minecraft.biologydictionary.platform.mixin.GuiGraphicsIMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

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
        this.debug = false;
    }

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

    public Minecraft getMinecraft()     { return minecraft; }
    public CommonScreen getScreen()     { return screen; }
    public GuiGraphics getGuiGraphics() { return guiGraphics; }
    public float getMouseX()            { return mouseX; }
    public float getMouseY()            { return mouseY; }
    public float getTickDelta()         { return tickDelta; }
    public Font getFont()               { return screen.getFont(); }
    public float getZ()                 { return screen.getZ(); }
    public boolean isDebug()            { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }

    public AutoCloseable scaleOnce(float size) {
        return new ScaleRAII(this, size);
    }

    public void renderText(Component component, int color, float x, float y) {
        getGuiGraphics().drawString(getFont(), component, (int) x, (int) y, color, false);
    }

    public void renderCenteredText(Component component, int color, float x, float y) {
        getGuiGraphics().drawString(getFont(), component, (int) x - getFont().width(component) / 2, (int) y, color, false);
    }

    public void renderHorizontalLine(int color, float width, float z, float y, float left, float right) {
        renderRectangle(color, z, left, y, right, y + width);
    }

    public void renderVerticalLine(int color, float width, float z, float x, float top, float bottom) {
        renderRectangle(color, z, x, top, x + width, bottom);
    }

    public void renderRectangle(int color, float width, float z, float left, float top, float right, float bottom) {
        renderHorizontalLine(color, width, z, top, left, right);
        renderHorizontalLine(color, width, z, bottom - width, left, right);
        renderVerticalLine(color, width, z, left, top, bottom);
        renderVerticalLine(color, width, z, right - width, top, bottom);
    }

    /**
     * @see net.minecraft.client.gui.GuiGraphics#fill(RenderType, int, int, int, int, int, int)
     */
    public void renderRectangle(int color, float z, float left, float top, float right, float bottom) {
        getGuiGraphics().fill((int) left, (int) top, (int) right, (int) bottom, (int) z, color);
    }

    public void renderTexture(ResourceLocation texture,
                              float resourceWidth, float resourceHeight,
                              float textureLeft, float textureTop,
                              float z, float left, float top, float width, float height) {
        renderTexture(texture, resourceWidth, resourceHeight,
                textureLeft, textureTop, textureLeft + width, textureTop + height,
                z, left, top, left + width,  top + height);
    }

    /**
     * @see net.minecraft.client.gui.GuiGraphics#innerBlit(ResourceLocation, int, int, int, int, int, float, float, float, float)
     */
    public void renderTexture(ResourceLocation texture,
                              float resourceWidth, float resourceHeight,
                              float textureLeft, float textureTop, float textureRight, float textureBottom,
                              float z, float left, float top, float right, float bottom) {
        ((GuiGraphicsIMixin) getGuiGraphics()).callInnerBlit(texture, (int) left, (int) right, (int) top, (int) bottom, (int) z,
                textureLeft / resourceWidth, textureRight / resourceWidth,
                textureTop / resourceHeight, textureBottom / resourceHeight);
    }

    /**
     * @see net.minecraft.client.gui.screens.inventory.InventoryScreen#renderEntityInInventoryFollowsMouse(GuiGraphics, int, int, int, float, float, LivingEntity)
     * @see net.minecraft.client.gui.screens.inventory.InventoryScreen#renderEntityInInventory(GuiGraphics, int, int, int, Quaternionf, Quaternionf, LivingEntity)
     */
    public void renderEntity(Entity entity, float midX, float bottom, float scale,
                             float rotateX, float rotateY, boolean lightFromBelow) {
        float sign = lightFromBelow ? -1 : 1;
        GuiGraphics guiGraphics = getGuiGraphics();
        Quaternionf quaternionf = new Quaternionf()
                .rotateX(rotateY * sign)
                .rotateY((float) Math.PI - rotateX * sign)
                .rotateZ(lightFromBelow ? 0 : (float) Math.PI);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(midX, bottom, 50.0);
        // control light by x and y
        guiGraphics.pose().mulPoseMatrix(new Matrix4f().scaling(scale * sign, scale * sign, -scale));
        guiGraphics.pose().mulPose(quaternionf);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        entityRenderDispatcher.setRenderShadow(false);
        entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, guiGraphics.pose(), guiGraphics.bufferSource(), 0xF000F0);
        guiGraphics.flush();
        entityRenderDispatcher.setRenderShadow(true);
        guiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    /**
     * @see net.minecraft.client.gui.screens.inventory.PageButton#playDownSound(SoundManager)
     */
    public void playScreenSound(SoundEvent sound, float volume, float pitch) {
        getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    private static class ScaleRAII implements AutoCloseable {
        private final GuiGraphics guiGraphics;

        private ScaleRAII(ScreenRenderingContext ctx, float size) {
            guiGraphics = ctx.getGuiGraphics();
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(size, size, size);
        }

        @Override
        public void close() throws Exception {
            guiGraphics.pose().popPose();
        }
    }
}
