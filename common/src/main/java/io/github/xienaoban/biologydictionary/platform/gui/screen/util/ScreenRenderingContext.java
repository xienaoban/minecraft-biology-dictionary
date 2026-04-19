package io.github.xienaoban.biologydictionary.platform.gui.screen.util;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.mixin.rendering.GuiGraphicsIMixin;
import io.github.xienaoban.biologydictionary.mixin.rendering.ScreenIMixin;
import io.github.xienaoban.biologydictionary.platform.gui.TextureInfo;
import io.github.xienaoban.biologydictionary.platform.gui.screen.CommonScreen;
import io.github.xienaoban.biologydictionary.platform.gui.screen.ElementScreen;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector2ic;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public final class ScreenRenderingContext {
    private final Screen screen;

    private final Minecraft client;
    private GuiGraphics guiGraphics;
    private float screenScale, reciprocalScreenScale;
    private float mouseX, mouseY;
    private float tickDelta;
    private boolean debug;

    public ScreenRenderingContext(Screen screen) {
        this.client = Objects.requireNonNull(ClientUtils.getClient());
        this.screen = screen;
        this.screenScale = 1F;
        this.reciprocalScreenScale = 1F;
        this.debug = false;
    }

    /**
     * We don't use the mouseX and mouseY parameters because they are int.
     * @see net.minecraft.client.renderer.GameRenderer#render(float, long, boolean)
     *
     * @param mouseX not used
     * @param mouseY not used
     */
    public void update(GuiGraphics guiGraphics, float screenScale, float reciprocalScreenScale, int mouseX, int mouseY, float tickDelta) {
        this.guiGraphics = guiGraphics;
        this.screenScale = screenScale;
        this.reciprocalScreenScale = reciprocalScreenScale;
        this.tickDelta = tickDelta;

        this.mouseX = (float) client.mouseHandler.xpos() * (float) client.getWindow().getGuiScaledWidth() / (float) client.getWindow().getScreenWidth();
        this.mouseY = (float) client.mouseHandler.ypos() * (float) client.getWindow().getGuiScaledHeight() / (float) client.getWindow().getScreenHeight();
        assert mouseX == (int) this.mouseX && mouseY == (int) this.mouseY;

        this.mouseX *= reciprocalScreenScale;
        this.mouseY *= reciprocalScreenScale;
    }

    public Minecraft getClient()        { return client; }
    public Screen getScreen()           { return screen; }
    public GuiGraphics getGuiGraphics() { return guiGraphics; }
    public float getMouseX()            { return mouseX; }
    public float getMouseY()            { return mouseY; }
    public float getTickDelta()         { return tickDelta; }
    public Font getFont()               { return ((ScreenIMixin) screen).biologydictionary$getFont(); }
    public float getZ()                 { return getCommonScreen().getZ(); }
    public boolean isDebug()            { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }

    public CommonScreen getCommonScreen()           { return (CommonScreen) screen; }
    public ElementScreen getElementScreen()           { return (ElementScreen) screen; }

    public PoseStack getPose() {
        return getGuiGraphics().pose();
    }

    public MultiBufferSource.BufferSource bufferSource() {
        return getGuiGraphics().bufferSource();
    }

    public GuiGraphics.ScissorStack getScissorStack() {
        return ((GuiGraphicsIMixin) getGuiGraphics()).biologydictionary$getScissorStack();
    }

    public ScaleRAII scaleOnce(float size) {
        return new ScaleRAII(this, size);
    }

    public ScaleRAII scaleOnce(float size, float z) {
        return new ScaleRAII(this, size, z);
    }

    //=======================================================================================
    // Rendering texts.
    //=======================================================================================

    public int calcTextWidth(Component component) {
        return getFont().width(component);
    }

    public int calcTextWidth(FormattedCharSequence text) {
        return getFont().width(text);
    }

    /**
     * Render text with float precision.
     *
     * @see net.minecraft.client.gui.GuiGraphics#drawString(net.minecraft.client.gui.Font, net.minecraft.network.chat.Component, int, int, int, boolean)
     * @see net.minecraft.client.gui.GuiGraphics#drawString(net.minecraft.client.gui.Font, net.minecraft.util.FormattedCharSequence, int, int, int, boolean)
     */
    public void renderText(FormattedCharSequence text, int color, float z, float x, float y) {
        Font font = getFont();
        PoseStack pose = getPose();
        pose.pushPose();
        pose.translate(0, 0, z);
        font.drawInBatch(text, x, y, color, false, pose.last().pose(), bufferSource(),
                Font.DisplayMode.NORMAL, 0, 15728880);
        pose.popPose();
        ((io.github.xienaoban.biologydictionary.mixin.rendering.GuiGraphicsIMixin) getGuiGraphics()).biologydictionary$invokeFlushIfUnmanaged();
    }

    public void renderText(FormattedCharSequence text, int color, float size, float z, float x, float y) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderText(text, color, z, x / size, y / size);
        }
    }

    public void renderText(Component component, int color, float z, float x, float y) {
        renderText(component.getVisualOrderText(), color, z, x, y);
    }

    public void renderText(Component component, int color, float size, float z, float x, float y) {
        renderText(component.getVisualOrderText(), color, size, z, x, y);
    }

    public void renderCenteredText(Component component, int color, float z, float x, float y) {
        renderText(component, color, z, x - calcTextWidth(component) / 2.0F, y);
    }

    public void renderCenteredText(Component component, int color, float size, float z, float x, float y) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderCenteredText(component, color, z, x / size, y / size);
        }
    }

    public void renderRightAlignedText(Component component, int color, float z, float x, float y) {
        renderText(component, color, z, x - calcTextWidth(component), y);
    }

    public void renderRightAlignedText(Component component, int color, float size, float z, float x, float y) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderRightAlignedText(component, color, z, x / size, y / size);
        }
    }

    //=======================================================================================
    // Rendering geometries.
    //=======================================================================================

    public void renderHorizontalLine(int color, float width, float z, float y, float left, float right) {
        renderRectangle(color, z, left, y - width / 2.0F, right, y + width / 2.0F);
    }

    public void renderVerticalLine(int color, float width, float z, float x, float top, float bottom) {
        renderRectangle(color, z, x - width / 2.0F, top, x + width / 2.0F, bottom);
    }

    /**
     * Another choice is:
     * {@snippet :
     *     renderHorizontalLine(color, width, z, top, left, right);
     *     renderHorizontalLine(color, width, z, bottom - width, left, right);
     *     renderVerticalLine(color, width, z, left, top, bottom);
     *     renderVerticalLine(color, width, z, right - width, top, bottom);
     * }
     */
    public void renderRectangle(int color, float width, float z, float left, float top, float right, float bottom) {
         renderRectangle(color, z, left, top, right, top + width);
         renderRectangle(color, z, left, bottom - width, right, bottom);
         renderRectangle(color, z, left, top, left + width, bottom);
         renderRectangle(color, z, right - width, top, right, bottom);
    }

    /**
     * Render a filled rectangle with float precision.
     *
     * @see net.minecraft.client.gui.GuiGraphics#fill(net.minecraft.client.renderer.RenderType, int, int, int, int, int, int)
     */
    public void renderRectangle(int color, float z, float left, float top, float right, float bottom) {
        org.joml.Matrix4f matrix4f = getPose().last().pose();
        VertexConsumer vertexConsumer = bufferSource().getBuffer(RenderType.gui());
        vertexConsumer.vertex(matrix4f, left, top, z).color(color).endVertex();
        vertexConsumer.vertex(matrix4f, left, bottom, z).color(color).endVertex();
        vertexConsumer.vertex(matrix4f, right, bottom, z).color(color).endVertex();
        vertexConsumer.vertex(matrix4f, right, top, z).color(color).endVertex();
        ((io.github.xienaoban.biologydictionary.mixin.rendering.GuiGraphicsIMixin) getGuiGraphics()).biologydictionary$invokeFlushIfUnmanaged();
    }

    public void renderTexture(TextureInfo texture,
                              float textureLeft, float textureTop,
                              float z, float left, float top, float width, float height) {
        renderTexture(texture,
                textureLeft, textureTop, textureLeft + width, textureTop + height,
                z, left, top, left + width,  top + height);
    }

    /**
     * Render a textured rectangle with float precision.
     *
     * @see net.minecraft.client.gui.GuiGraphics#innerBlit(net.minecraft.resources.ResourceLocation, int, int, int, int, int, float, float, float, float)
     */
    public void renderTexture(TextureInfo texture,
                              float textureLeft, float textureTop, float textureRight, float textureBottom,
                              float z, float left, float top, float right, float bottom) {
        // Calculate UV coordinates
        float u0 = textureLeft / texture.width();
        float v0 = textureTop / texture.height();
        float u1 = textureRight / texture.width();
        float v1 = textureBottom / texture.height();

        // Set texture and shader
        RenderSystem.setShaderTexture(0, texture.location());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        // Build vertices using Tesselator (same as innerBlit)
        org.joml.Matrix4f matrix4f = getPose().last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix4f, left, top, z).uv(u0, v0).endVertex();
        bufferBuilder.vertex(matrix4f, left, bottom, z).uv(u0, v1).endVertex();
        bufferBuilder.vertex(matrix4f, right, bottom, z).uv(u1, v1).endVertex();
        bufferBuilder.vertex(matrix4f, right, top, z).uv(u1, v0).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    //=======================================================================================
    // Rendering items and sprites.
    //=======================================================================================

    public void renderItem(ItemStack itemStack, float left, float top) {
        getGuiGraphics().renderFakeItem(itemStack, (int) left, (int) top);
    }

    public void renderItem(ItemStack itemStack, float size, float left, float top) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderItem(itemStack, left / size, top / size);
        }
    }

    /**
     * @see net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen#renderIcons(net.minecraft.client.gui.GuiGraphics, int, int, java.lang.Iterable, boolean)
     */
    public void renderEffect(Holder<MobEffect> effect, float left, float top) {
        TextureAtlasSprite sprite = getClient().getMobEffectTextures().get(effect.value());
        getGuiGraphics().blit((int) left, (int) top, 0, 18, 18, sprite);
    }

    public void renderEffect(Holder<MobEffect> effect, float size, float left, float top) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderEffect(effect, left / size, top / size);
        }
    }

    //=======================================================================================
    // Rendering tooltips.
    //=======================================================================================

    public void renderComponentTooltip(List<Component> texts, float leftX, float topY) {
        renderTooltipTexts(texts, 1F, leftX, topY);
    }

    public void renderComponentTooltip(List<Component> texts, float size, float leftX, float topY) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderTooltipTexts(texts, size, leftX, topY);
        }
    }

    public void renderComponentTooltipCentered(List<Component> texts, float midX, float topY) {
        int maxLength = texts.stream().mapToInt(this::calcTextWidth).max().orElse(20);
        renderComponentTooltip(texts, midX - (maxLength + 6) / 2F, topY);
    }

    public void renderComponentTooltipCenteredVanilla(List<Component> texts, float midX, float topY) {
        int maxLength = texts.stream().mapToInt(this::calcTextWidth).max().orElse(20);
        getGuiGraphics().renderComponentTooltip(getFont(), texts, (int) (midX - (maxLength + 6) / 2F), (int) topY);
    }

    public void renderComponentTooltipCentered(List<Component> texts, float size, float midX, float topY) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            int maxLength = texts.stream().mapToInt(this::calcTextWidth).max().orElse(20);
            renderTooltipTexts(texts, size, midX - (maxLength + 6) * size / 2F, topY);
        }
    }

    public void renderLinedTooltip(List<FormattedCharSequence> lines, float leftX, float topY) {
        renderTooltipLines(lines, 1F, leftX, topY);
    }

    public void renderLinedTooltip(List<FormattedCharSequence> lines, float size, float leftX, float topY) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderTooltipLines(lines, size, leftX, topY);
        }
    }

    public void renderLinedTooltipCentered(List<FormattedCharSequence> lines, float midX, float topY) {
        int maxLength = lines.stream().mapToInt(this::calcTextWidth).max().orElse(20);
        renderTooltipLines(lines, 1F, midX - (maxLength + 6) / 2F, topY);
    }

    public void renderLinedTooltipCentered(List<FormattedCharSequence> lines, float size, float midX, float topY) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            int maxLength = lines.stream().mapToInt(this::calcTextWidth).max().orElse(20);
            renderTooltipLines(lines, size, midX - (maxLength + 6) * size / 2F, topY);
        }
    }

    private void renderTooltipTexts(List<Component> texts, float size, float x, float y) {
        Font font = getFont();
        List<FormattedCharSequence> list = texts.stream()
                .flatMap(c -> {
                    List<FormattedCharSequence> lines = font.split(c, Widget.TOOLTIP_WIDTH);
                    return lines.isEmpty() ? Stream.of(TextUtils.empty().getVisualOrderText()) : lines.stream();
                })
                .toList();
        renderTooltipLines(list, size, x, y);
    }

    /**
     * Similar to GuiGraphics#renderTooltip.
     * The only difference is that we give the argument {@code size}
     * to calculate the real gui width and gui height.
     *
     * @see net.minecraft.client.gui.GuiGraphics#renderComponentTooltip(net.minecraft.client.gui.Font, java.util.List, int, int)
     * @see net.minecraft.client.gui.GuiGraphics#renderTooltipInternal(net.minecraft.client.gui.Font, java.util.List, int, int, net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner)
     */
    private void renderTooltipLines(List<FormattedCharSequence> lines, float size, float x, float y) {
        Font font = getFont();
        List<ClientTooltipComponent> list = lines.stream()
                .map(ClientTooltipComponent::create)
                .toList();
        ClientTooltipPositioner clientTooltipPositioner = DefaultTooltipPositioner.INSTANCE;

        if (!list.isEmpty()) {
            int width = 0;
            int height = list.size() == 1 ? -2 : 0;

            for (ClientTooltipComponent clientTooltipComponent : list) {
                int m = clientTooltipComponent.getWidth(font);
                if (m > width) {
                    width = m;
                }
                height += clientTooltipComponent.getHeight();
            }

            int n = width;
            int o = height;
            Vector2ic vector2ic = clientTooltipPositioner.positionTooltip((int) (getGuiGraphics().guiWidth() / size), (int) (getGuiGraphics().guiHeight() / size), (int) (x / size - 8), (int) (y / size + 16), n, o);
            int p = vector2ic.x();
            int q = vector2ic.y();
            getPose().pushPose();
            int r = 400;
            getGuiGraphics().drawManaged(() -> TooltipRenderUtil.renderTooltipBackground(getGuiGraphics(), p, q, n, o, r));
            getPose().translate(0.0F, 0.0F, 400.0F);
            int s = q;

            for (int t = 0; t < list.size(); t++) {
                ClientTooltipComponent clientTooltipComponent2 = list.get(t);
                clientTooltipComponent2.renderText(font, p, s, getPose().last().pose(), bufferSource());
                s += clientTooltipComponent2.getHeight() + (t == 0 ? 2 : 0);
            }

            s = q;

            for (int t = 0; t < list.size(); t++) {
                ClientTooltipComponent clientTooltipComponent2 = list.get(t);
                clientTooltipComponent2.renderImage(font, p, s, getGuiGraphics());
                s += clientTooltipComponent2.getHeight() + (t == 0 ? 2 : 0);
            }

            getPose().popPose();
        }
    }

    //=======================================================================================
    // Rendering entities.
    //=======================================================================================

    public void renderEntityBottomed(Entity entity, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, float forceScale) {
        renderEntity(entity, left, top, right, bottom, rotateX, rotateY, forceScale, 1F);
    }

    public void renderEntityCentered(Entity entity, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY) {
        renderEntity(entity, left, top, right, bottom, rotateX, rotateY, -1, 1.9F);
    }

    public void renderEntityCentered(Entity entity, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, float forceScale) {
        renderEntity(entity, left, top, right, bottom, rotateX, rotateY, forceScale, 1.9F);
    }

    public void renderEntityCentered(Entity entity, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, int silhouetteColor) {
        renderEntity(entity, left, top, right, bottom, rotateX, rotateY, -1, 1.9F, silhouetteColor);
    }

    public void renderEntityCentered(Entity entity, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, float forceScale, int silhouetteColor) {
        renderEntity(entity, left, top, right, bottom, rotateX, rotateY, forceScale, 1.9F, silhouetteColor);
    }

    private void renderEntity(Entity entity, float left, float top, float right, float bottom,
                              float rotateX, float rotateY, float forceScale, float internalOffset) {
        renderEntity(entity, left, top, right, bottom, rotateX, rotateY, forceScale, internalOffset, 0);
    }

    /**
     * @see net.minecraft.client.gui.screens.inventory.InventoryScreen#renderEntityInInventoryFollowsMouse(net.minecraft.client.gui.GuiGraphics, int, int, int, float, float, net.minecraft.world.entity.LivingEntity)
     */
    private void renderEntity(Entity entity, float left, float top, float right, float bottom,
                              float rotateX, float rotateY, float forceScale, float internalOffset, int silhouetteColor) {
        final float width = right - left;
        final float height = bottom - top;
        final float entityWidth = entity.getBbWidth();
        final float entityHeight = entity.getBbHeight();

        // This function does not compatible with guiGraphics.pose().scale(size, size).
        getGuiGraphics().enableScissor((int) (left * screenScale), (int) (top * screenScale), (int) (right * screenScale), (int) (bottom * screenScale));

        final float scale;
        if (forceScale < 0) {
            float vw;
            if (entityWidth > 1.8F) {
                vw = entityWidth / 1.4F;
            } else {
                vw = (float) Math.log(1.1F + entityWidth);
            }
            float vh;
            if (entityHeight > 2.2F) {
                vh = entityHeight / 1.8F;
            } else {
                vh = (float) Math.log(1.1F + entityHeight);
            }
            scale = Math.min(width / vw, height / vh) / 2.2F;
        } else {
            scale = Math.min(width / entityWidth, height / entityHeight) / 1.5F * forceScale;
        }

        float posX = (left + right) / 2.0F;
        float posY = (top + bottom) / 2.0F;

        float sc = 1F;
        float finalScale = scale / sc;

        Quaternionf quaternionf = new Quaternionf().rotateX(-rotateY * 20F * (float) (Math.PI / 180F));
        Quaternionf quaternionf2 = new Quaternionf().rotateY( rotateX * 20F * (float) (Math.PI / 180F));
        Quaternionf quaternionf3 = new Quaternionf().rotateZ((float) Math.PI);
        quaternionf.mul(quaternionf2).mul(quaternionf3);

        Vector3f vector3f = new Vector3f(0F, entity.getBbHeight() / internalOffset + 0.0625F * sc, 0F);

        PoseStack poseStack = getPose();
        poseStack.pushPose();
        poseStack.translate(posX, posY, 50F);
        poseStack.scale(finalScale, finalScale, finalScale);
        poseStack.translate(vector3f.x(), vector3f.y(), vector3f.z());
        poseStack.mulPose(quaternionf);

        Lighting.setupForFlatItems();
        RenderSystem.setShaderLights(new Vector3f(-1.5F, -2.0F, -2F).normalize(), new Vector3f(-1.5F, -2.0F, 2F).normalize());
        EntityRenderDispatcher entityRenderDispatcher = getClient().getEntityRenderDispatcher();

        float prevYRot = entity.getYRot();
        float prevXRot = entity.getXRot();
        float prevYBodyRot = 0, prevYBodyRotO = 0, prevYHeadRot = 0, prevYHeadRotO = 0;
        if (entity instanceof LivingEntity living) {
            prevYBodyRot = living.yBodyRot;
            prevYBodyRotO = living.yBodyRotO;
            prevYHeadRot = living.yHeadRot;
            prevYHeadRotO = living.yHeadRotO;
            living.yBodyRot = 0F;
            living.yBodyRotO = 0F;
            living.yHeadRot = 0F;
            living.yHeadRotO = 0F;
        }
        entity.setYRot(0F);
        entity.setXRot(0F);

        entityRenderDispatcher.setRenderShadow(false);
        if (silhouetteColor == 0) {
            RenderSystem.runAsFancy(
                    () -> entityRenderDispatcher.render(entity, 0D, 0D, 0D, 0F, 1F, getPose(), bufferSource(), 15728880)
            );
        } else {
            SilhouetteMultiBufferSource silhouetteBuffer = new SilhouetteMultiBufferSource(silhouetteColor);
            RenderSystem.runAsFancy(
                    () -> entityRenderDispatcher.render(entity, 0D, 0D, 0D, 0F, 1F, getPose(), silhouetteBuffer, 15728880)
            );
            getGuiGraphics().flush();
            silhouetteBuffer.end();
        }
        getGuiGraphics().flush();
        entityRenderDispatcher.setRenderShadow(true);

        entity.setYRot(prevYRot);
        entity.setXRot(prevXRot);
        if (entity instanceof LivingEntity living) {
            living.yBodyRot = prevYBodyRot;
            living.yBodyRotO = prevYBodyRotO;
            living.yHeadRot = prevYHeadRot;
            living.yHeadRotO = prevYHeadRotO;
        }
        poseStack.popPose();
        Lighting.setupFor3DItems();

        getGuiGraphics().disableScissor();

        if (isDebug() && width > 0 && height > 0) {
            final int color = 0xFFAAAAAA;
            renderRectangle(color, 0.6F, getZ(), left, top, right, bottom);
        }
    }

    public void renderPlayerFace(AbstractClientPlayer player, float left, float top) {
        renderPlayerFace(player, left, top, 8F);
    }

    /**
     * @see net.minecraft.client.gui.components.PlayerTabOverlay#render(net.minecraft.client.gui.GuiGraphics, int, net.minecraft.world.scores.Scoreboard, net.minecraft.world.scores.Objective)
     */
    public void renderPlayerFace(AbstractClientPlayer player, float left, float top, float size) {
        PlayerFaceRenderer.draw(getGuiGraphics(), player.getSkinTextureLocation(), (int) left, (int) top, (int) size, /* show hat */ true, /* upsideDown */ false);
    }
}
