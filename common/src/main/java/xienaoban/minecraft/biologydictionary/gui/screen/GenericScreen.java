package xienaoban.minecraft.biologydictionary.gui.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public abstract class GenericScreen extends Screen {

    protected GenericScreen(Component component) {
        super(component);
    }

    public static PoseStack poseScaleOn(float x, float y, float z) {
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.scale(x, y, z);
        RenderSystem.applyModelViewMatrix();
        return poseStack;
    }

    public static void poseScaleOff(PoseStack poseStack) {
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public static void renderEntityAuto(Entity entity, int x0, int y0, int x1, int y1, float mouseX, float mouseY) {
        AABB box = entity.getBoundingBoxForCulling();
        double ew = box.getXsize(), eh = box.getYsize();
        if (ew > eh) {
            ew = Math.max(ew, 1);
        }
        else {
            eh = Math.max(eh, 1);
        }
        int rw = x1 - x0, rh = y1 - y0;
        int size = (int) (Math.min(rw / ew, rh / eh) * 0.85);
        renderEntityBrighter(entity, size, x0 + x1 >> 1, y1, mouseX, mouseY);
    }

    public static void renderEntityByWidth(Entity entity, int width, int x, int y, float mouseX, float mouseY) {
        AABB box = entity.getBoundingBoxForCulling();
        renderEntityBrighter(entity, (int) (width / box.getXsize()), x, y, mouseX, mouseY);
    }

    public static void renderEntityByHeight(Entity entity, int height, int x, int y, float mouseX, float mouseY) {
        AABB box = entity.getBoundingBoxForCulling();
        renderEntityBrighter(entity, (int) (height / box.getYsize()), x, y, mouseX, mouseY);
    }

    /**
     * Draw an entity.
     * This method is similar to InventoryScreen.drawEntity(), but there are some differences:
     * <p>1. It can render any Entity, not only LivingEntity. </p>
     * <p>2. The entity it renders is brighter (but it brings some lighting bugs...). </p>
     * <p>3. It can't recognize the yaw of LivingEntity, so don’t use it to render a rotating LivingEntity. </p>
     * @see InventoryScreen#renderEntityInInventory(int, int, int, float, float, LivingEntity)
     */
    @SuppressWarnings("deprecation")
    public static void renderEntityBrighter(Entity entity, int size, int x, int y, float mouseX, float mouseY) {
        float f = (float)Math.atan(mouseX / 40.0F);
        float g = (float)Math.atan(mouseY / 40.0F);
        float fSize = -size;
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate(x, y, 1050.0F);
        poseStack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack poseStack2 = new PoseStack();
        poseStack2.translate(0.0D, 0.0D, 1000.0D);
        poseStack2.scale(fSize, fSize, fSize);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(0.0F);
        Quaternion quaternion2 = Vector3f.XP.rotationDegrees(g * 20.0F);
        Quaternion quaternion3 = Vector3f.YP.rotationDegrees(entity.getViewYRot(0.0F) - f * 40.0F);
        quaternion.mul(quaternion2);
        quaternion.mul(quaternion3);
        poseStack2.mulPose(quaternion);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion2.conj();
        entityRenderDispatcher.overrideCameraOrientation(quaternion2);
        entityRenderDispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, poseStack2, bufferSource, 0x00F000F0));   // 0x00F000F0
        bufferSource.endBatch();
        entityRenderDispatcher.setRenderShadow(true);
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    /**
     * @see InventoryScreen#renderEntityInInventory(int, int, int, float, float, LivingEntity)
     */
    public static void renderEntityGeneric(Entity entity,float size, float x, float y, float rotateX, float rotateY, float rotateZ) {
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate(x, y, 1010.0);
        poseStack.scale(1.0f, 1.0f, -1.0f);
        RenderSystem.applyModelViewMatrix();
        PoseStack poseStack2 = new PoseStack();
        poseStack2.translate(0.0, 0.0, 1000.0);
        poseStack2.scale(size, size, size);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0f + rotateZ);
        Quaternion quaternion2 = Vector3f.XP.rotationDegrees(rotateX);
        Quaternion quaternion3 = Vector3f.YP.rotationDegrees(rotateY);
        quaternion.mul(quaternion2);
        quaternion.mul(quaternion3);
        poseStack2.mulPose(quaternion);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion3.conj();
        entityRenderDispatcher.overrideCameraOrientation(quaternion3);
        entityRenderDispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, poseStack2, bufferSource, 0xF000F0));
        bufferSource.endBatch();
        entityRenderDispatcher.setRenderShadow(true);
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    public static void renderRectangle(PoseStack poseStack, int color, float z, float x0, float y0, float x1, float y1) {
        renderQuadrilateral(poseStack, color, z, x0, y0, x1, y0, x0, y1, x1, y1);
    }

    public static void renderRectangle(PoseStack poseStack, int color, float radius, float z, float x0, float y0, float x1, float y1) {
        renderHorizontalLine(poseStack, color, radius, z, x0, x1, y0);
        renderHorizontalLine(poseStack, color, radius, z, x0, x1, y1);
        renderVerticalLine(poseStack, color, radius, z, x0, y0, y1);
        renderVerticalLine(poseStack, color, radius, z, x1, y0, y1);
    }

    public static void renderHorizontalLine(PoseStack poseStack, int color, float radius, float z, float x0, float x1, float y) {
        renderQuadrilateral(poseStack, color, z, x0, y - radius, x1, y - radius, x0, y + radius, x1, y + radius);
    }

    public static void renderVerticalLine(PoseStack poseStack, int color, float radius, float z, float x, float y0, float y1) {
        renderQuadrilateral(poseStack, color, z, x - radius, y0, x + radius, y0, x - radius, y1, x + radius, y1);
    }

    /**
     * @see GuiComponent#innerFill(Matrix4f, int, int, int, int, int)
     */
    public static void renderQuadrilateral(PoseStack poseStack, int color, float z,
                                         float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
        Matrix4f model = poseStack.last().pose();
        int a = color >> 24 & 255;
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(model, x0, y0, z).color(r, g, b, a).endVertex();    // The four vertices must
        bufferBuilder.vertex(model, x2, y2, z).color(r, g, b, a).endVertex();    // be added in clockwise
        bufferBuilder.vertex(model, x3, y3, z).color(r, g, b, a).endVertex();    // or counterclockwise
        bufferBuilder.vertex(model, x1, y1, z).color(r, g, b, a).endVertex();    // order.
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    /**
     * Draws a textured quadrilateral from a region in a texture.
     *
     * @param poseStack the matrix stack used for rendering
     * @param tw the width of the entire texture
     * @param th the height of the entire texture
     * @param w the width of the quadrilateral and the texture region
     * @param h the height of the quadrilateral and the texture region
     * @param z the Z coordinate of the quadrilateral
     * @param x the left-most coordinate of the quadrilateral
     * @param y the top-most coordinate of the quadrilateral
     * @param u the left-most coordinate of the texture region
     * @param v the top-most coordinate of the texture region
     */
    public static void renderTextureNormally(PoseStack poseStack, float tw, float th, float w, float h, float z, float x, float y, float u, float v) {
        renderTextureQuadrilateral(poseStack, tw, th, z, x, y, x + w, y, x, y + h, x + w, y + h, u, v, u + w, v, u, v + h, u + w, v + h);
    }

    /**
     * Draws a textured quadrilateral from a region in a texture.
     *
     * @param poseStack the matrix stack used for rendering
     * @param tw the width of the entire texture
     * @param th the height of the entire texture
     * @param z the Z coordinate of the quadrilateral
     * @param x0 the left-most coordinate of the quadrilateral
     * @param y0 the top-most coordinate of the quadrilateral
     * @param x1 the right-most coordinate of the quadrilateral
     * @param y1 the bottom-most coordinate of the quadrilateral
     * @param u0 the left-most coordinate of the texture region
     * @param v0 the top-most coordinate of the texture region
     * @param u1 the right-most coordinate of the texture region
     * @param v1 the bottom-most coordinate of the texture region
     */
    public static void renderTextureNormally(PoseStack poseStack, float tw, float th, float z,
                                           float x0, float y0, float x1, float y1,
                                           float u0, float v0, float u1, float v1) {
        renderTextureQuadrilateral(poseStack, tw, th, z, x0, y0, x1, y0, x0, y1, x1, y1, u0, v0, u1, v0, u0, v1, u1, v1);
    }

    public static void renderTextureFlippedHorizontally(PoseStack poseStack, float tw, float th, float w, float h, float z, float x, float y, float u, float v) {
        renderTextureQuadrilateral(poseStack, tw, th, z, x, y, x + w, y, x, y + h, x + w, y + h, u + w, v, u, v, u + w, v + h, u, v + h);
    }

    /**
     * Draws a textured quadrilateral from a region in a horizontally flipped texture.
     */
    public static void renderTextureFlippedHorizontally(PoseStack poseStack, float tw, float th, float z,
                                                      float x0, float y0, float x1, float y1,
                                                      float u0, float v0, float u1, float v1) {
        renderTextureQuadrilateral(poseStack, tw, th, z, x0, y0, x1, y0, x0, y1, x1, y1, u1, v0, u0, v0, u1, v1, u0, v1);
    }

    /**
     * Draws a textured quadrilateral from a region in a vertically flipped texture.
     */
    public static void renderTextureFlippedVertically(PoseStack poseStack, float tw, float th, float z,
                                                    float x0, float y0, float x1, float y1,
                                                    float u0, float v0, float u1, float v1) {
        renderTextureQuadrilateral(poseStack, tw, th, z, x0, y0, x1, y0, x0, y1, x1, y1, u0, v1, u1, v1, u0, v0, u1, v0);
    }

    /**
     * Draws a textured quadrilateral from a region in a 180 rotated texture.
     */
    public static void renderTextureRotated180(PoseStack poseStack, float tw, float th, float w, float h, float z, float x, float y, float u, float v) {
        renderTextureQuadrilateral(poseStack, tw, th, z, x, y, x + w, y, x, y + h, x + w, y + h, u + w, v + h, u, v + h, u + w, v, u, v);
    }

    /**
     * Draws a textured quadrilateral from a region in a 180 rotated texture.
     */
    public static void renderTextureRotated180(PoseStack poseStack, float tw, float th, float z,
                                             float x0, float y0, float x1, float y1,
                                             float u0, float v0, float u1, float v1) {
        renderTextureQuadrilateral(poseStack, tw, th, z, x0, y0, x1, y0, x0, y1, x1, y1, u1, v1, u0, v1, u1, v0, u0, v0);
    }

    /**
     * @see GuiComponent#innerBlit(Matrix4f, int, int, int, int, int, float, float, float, float)
     */
    public static void renderTextureQuadrilateral(PoseStack poseStack, float textureWidth, float textureHeight, float z,
                                                float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3,
                                                float u0, float v0, float u1, float v1, float u2, float v2, float u3, float v3) {
        u0 /= textureWidth; v0 /= textureHeight;
        u1 /= textureWidth; v1 /= textureHeight;
        u2 /= textureWidth; v2 /= textureHeight;
        u3 /= textureWidth; v3 /= textureHeight;
        Matrix4f model = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(model, x0, y0, z).uv(u0, v0).endVertex();      // The four vertices must
        bufferBuilder.vertex(model, x2, y2, z).uv(u2, v2).endVertex();      // be added in clockwise
        bufferBuilder.vertex(model, x3, y3, z).uv(u3, v3).endVertex();      // or counterclockwise
        bufferBuilder.vertex(model, x1, y1, z).uv(u1, v1).endVertex();      // order.
        BufferUploader.drawWithShader(bufferBuilder.end());
    }
}
