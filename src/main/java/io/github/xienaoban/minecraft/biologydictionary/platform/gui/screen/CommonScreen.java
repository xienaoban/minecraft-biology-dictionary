package io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public abstract class CommonScreen extends Screen {
    protected CommonScreen(Component component) { super(component); }

    public final void renderText(PoseStack poseStack, Component component, int color, float x, float y) {
        font.draw(poseStack, component, x, y, color);
    }

    public final void renderCenteredText(PoseStack poseStack, Component component, int color, float x, float y) {
        font.draw(poseStack, component, x - font.width(component) * 0.5F, y, color);
    }

    public final void renderHorizontalLine(PoseStack poseStack, int color, float radius, float z, float midY, float left, float right) {
        renderRectangle(poseStack, color, z, left, midY - radius, right, midY + radius);
    }

    public final void renderVerticalLine(PoseStack poseStack, int color, float radius, float z, float midX, float top, float bottom) {
        renderRectangle(poseStack, color, z, midX - radius, top, midX + radius, bottom);
    }

    public final void renderRectangle(PoseStack poseStack, int color, float radius, float z, float left, float top, float right, float bottom) {
        renderHorizontalLine(poseStack, color, radius, z, top, left - radius, right + radius);
        renderHorizontalLine(poseStack, color, radius, z, bottom, left - radius, right + radius);
        renderVerticalLine(poseStack, color, radius, z, left, top, bottom);
        renderVerticalLine(poseStack, color, radius, z, right, top, bottom);
    }

    /**
     * @see net.minecraft.client.gui.GuiComponent#fill(com.mojang.blaze3d.vertex.PoseStack, int, int, int, int, int, int)
     */
    public final void renderRectangle(PoseStack poseStack, int color, float z, float left, float top, float right, float bottom) {
        Matrix4f matrix4f = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, left, top, z).color(color).endVertex();
        bufferBuilder.vertex(matrix4f, left, bottom, z).color(color).endVertex();
        bufferBuilder.vertex(matrix4f, right, bottom, z).color(color).endVertex();
        bufferBuilder.vertex(matrix4f, right, top, z).color(color).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public final void setTexture(ResourceLocation texture) {
        RenderSystem.setShaderTexture(0, texture);
    }

    public final void renderTexture(PoseStack poseStack, float resourceWidth, float resourceHeight,
                                    float textureLeft, float textureTop,
                                    float z, float left, float top, float width, float height) {
        renderTexture(poseStack, resourceWidth, resourceHeight,
                textureLeft, textureTop, textureLeft + width, textureTop + height,
                z, left, top, left + width,  top + height);
    }

    /**
     * @see net.minecraft.client.gui.GuiComponent#innerBlit(Matrix4f, int, int, int, int, int, float, float, float, float)
     */
    public final void renderTexture(PoseStack poseStack, float resourceWidth, float resourceHeight,
                                    float textureLeft, float textureTop, float textureRight, float textureBottom,
                                    float z, float left, float top, float right, float bottom) {
        textureLeft /= resourceWidth; textureRight /= resourceWidth;
        textureTop /= resourceHeight; textureBottom /= resourceHeight;
        Matrix4f matrix4f = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix4f, left, top, z).uv(textureLeft, textureTop).endVertex();
        bufferBuilder.vertex(matrix4f, left, bottom, z).uv(textureLeft, textureBottom).endVertex();
        bufferBuilder.vertex(matrix4f, right, bottom, z).uv(textureRight, textureBottom).endVertex();
        bufferBuilder.vertex(matrix4f, right, top, z).uv(textureRight, textureTop).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }
}
