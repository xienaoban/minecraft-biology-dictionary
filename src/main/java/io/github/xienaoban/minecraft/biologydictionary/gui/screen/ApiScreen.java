package io.github.xienaoban.minecraft.biologydictionary.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.xienaoban.minecraft.biologydictionary.gui.screen.util.ScreenElement;
import io.github.xienaoban.minecraft.biologydictionary.gui.screen.util.ScreenRenderingContext;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

public abstract class ApiScreen extends Screen {
    protected final ScreenElement rootScreenElement;
    private final ScreenRenderingContext screenRenderingContext;

    protected ApiScreen(Component component) {
        super(component);
        this.rootScreenElement = new ScreenElement(null);
        this.screenRenderingContext = new ScreenRenderingContext(this);
    }

    @Override
    protected void init() {
        super.init();
        rootScreenElement.getBox().set(0, 0, width, height);
    }

    @Override
    public final void render(PoseStack poseStack, int mouseX, int mouseY, float tickDelta) {
        screenRenderingContext.update(poseStack, mouseX, mouseY, tickDelta);
        render(screenRenderingContext);
    }

    protected void render(ScreenRenderingContext ctx) {
        super.render(ctx.getPoseStack(), ctx.getMouseX(), ctx.getMouseY(), ctx.getTickDelta());
    }

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
}
