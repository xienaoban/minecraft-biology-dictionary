package io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.TextureInfo;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.CommonScreen;
import io.github.xienaoban.minecraft.biologydictionary.platform.mixin.GuiGraphicsIMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.function.Function;

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
     * @see net.minecraft.client.renderer.GameRenderer#render(net.minecraft.client.DeltaTracker, boolean)
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

    public MultiBufferSource.BufferSource getBufferSource() {
        return ((GuiGraphicsIMixin) getGuiGraphics()).getBufferSource();
    }

    public ScaleRAII scaleOnce(float size) {
        return new ScaleRAII(this, size);
    }

    /**
     * @see net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip#renderText(net.minecraft.client.gui.Font, int, int, org.joml.Matrix4f, net.minecraft.client.renderer.MultiBufferSource.BufferSource)
     */
    public void renderText(Component component, int color, float x, float y) {
        getFont().drawInBatch(component.getVisualOrderText(), x, y, color, false, getGuiGraphics().pose().last().pose(), getBufferSource(), Font.DisplayMode.NORMAL, 0, 0xF000F0);
    }

    public void renderText(Component component, int color, float size, float x, float y) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderText(component, color, x / size, y / size);
        }
    }

    public void renderCenteredText(Component component, int color, float x, float y) {
        renderText(component, color, x - getFont().width(component) / 2.0F, y);
    }

    public void renderCenteredText(Component component, int color, float size, float x, float y) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderCenteredText(component, color, x / size, y / size);
        }
    }

    public void renderHorizontalLine(int color, float width, float z, float y, float left, float right) {
        renderRectangle(color, z, left, y - width / 2.0F, right, y + width / 2.0F);
    }

    public void renderVerticalLine(int color, float width, float z, float x, float top, float bottom) {
        renderRectangle(color, z, x - width / 2.0F, top, x + width / 2.0F, bottom);
    }

    public void renderRectangle(int color, float width, float z, float left, float top, float right, float bottom) {
         renderRectangle(color, z, left, top, right, top + width);
         renderRectangle(color, z, left, bottom - width, right, bottom);
         renderRectangle(color, z, left, top, left + width, bottom);
         renderRectangle(color, z, right - width, top, right, bottom);
         /*
         Another choice is:
         renderHorizontalLine(color, width, z, top, left, right);
         renderHorizontalLine(color, width, z, bottom - width, left, right);
         renderVerticalLine(color, width, z, left, top, bottom);
         renderVerticalLine(color, width, z, right - width, top, bottom);
         */
    }

    /**
     * @see net.minecraft.client.gui.GuiGraphics#fill(net.minecraft.client.renderer.RenderType, int, int, int, int, int, int)
     */
    public void renderRectangle(int color, float z, float left, float top, float right, float bottom) {
        Matrix4f matrix4f = getGuiGraphics().pose().last().pose();
        VertexConsumer vertexConsumer = getBufferSource().getBuffer(RenderType.gui());
        vertexConsumer.addVertex(matrix4f, left, top, z).setColor(color);
        vertexConsumer.addVertex(matrix4f, left, bottom, z).setColor(color);
        vertexConsumer.addVertex(matrix4f, right, bottom, z).setColor(color);
        vertexConsumer.addVertex(matrix4f, right, top, z).setColor(color);
        /*
        Another choice is:
        getGuiGraphics().fill((int) left, (int) top, (int) right, (int) bottom, (int) z, color);
        But it only supports `int`.
        */
    }

    public void renderTexture(TextureInfo texture,
                              float textureLeft, float textureTop,
                              float z, float left, float top, float width, float height) {
        renderTexture(texture,
                textureLeft, textureTop, textureLeft + width, textureTop + height,
                z, left, top, left + width,  top + height);
    }

    /**
     * @see net.minecraft.client.gui.GuiGraphics#innerBlit(Function, ResourceLocation, int, int, int, int, float, float, float, float, int)
     */
    public void renderTexture(TextureInfo texture,
                              float textureLeft, float textureTop, float textureRight, float textureBottom,
                              float z, float left, float top, float right, float bottom) {
        float uvLeft = textureLeft / texture.width();
        float uvTop = textureTop / texture.height();
        float uvRight = textureRight / texture.width();
        float uvBottom = textureBottom / texture.height();

        RenderType renderType = RenderType.guiTextured(texture.location());
        Matrix4f matrix4f = getGuiGraphics().pose().last().pose();
        VertexConsumer vertexConsumer = getBufferSource().getBuffer(renderType).setColor(-1);
        vertexConsumer.addVertex(matrix4f, left,  top,    z).setUv(uvLeft,  uvTop).setColor(-1);
        vertexConsumer.addVertex(matrix4f, left,  bottom, z).setUv(uvLeft,  uvBottom).setColor(-1);
        vertexConsumer.addVertex(matrix4f, right, bottom, z).setUv(uvRight, uvBottom).setColor(-1);
        vertexConsumer.addVertex(matrix4f, right, top,    z).setUv(uvRight, uvTop).setColor(-1);
    }

    public void renderItem(ItemStack itemStack, float left, float top) {
        getGuiGraphics().renderFakeItem(itemStack, (int) left, (int) top);
    }

    public void renderItem(ItemStack itemStack, float size, float left, float top) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderItem(itemStack, left / size, top / size);
        }
    }

    /**
     * @see net.minecraft.client.gui.screens.inventory.InventoryScreen#renderEntityInInventoryFollowsMouse(net.minecraft.client.gui.GuiGraphics, int, int, int, int, int, float, float, float, net.minecraft.world.entity.LivingEntity)
     * @see net.minecraft.client.gui.screens.inventory.InventoryScreen#renderEntityInInventory(net.minecraft.client.gui.GuiGraphics, float, float, float, org.joml.Vector3f, org.joml.Quaternionf, org.joml.Quaternionf, net.minecraft.world.entity.LivingEntity)
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
        guiGraphics.pose().scale(scale * sign, scale * sign, -scale);
        guiGraphics.pose().mulPose(quaternionf);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        entityRenderDispatcher.setRenderShadow(false);
        guiGraphics.drawSpecial(
                multiBufferSource -> entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 1.0F, guiGraphics.pose(), multiBufferSource, 15728880)
        );
        guiGraphics.flush();
        entityRenderDispatcher.setRenderShadow(true);
        guiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    /**
     * @see net.minecraft.client.gui.screens.inventory.PageButton#playDownSound(net.minecraft.client.sounds.SoundManager)
     */
    public void playScreenSound(SoundEvent sound, float volume, float pitch) {
        getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }
}
