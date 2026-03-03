package io.github.xienaoban.biologydictionary.common.gui.screen.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.xienaoban.biologydictionary.common.gui.TextureInfo;
import io.github.xienaoban.biologydictionary.common.gui.screen.CommonScreen;
import io.github.xienaoban.biologydictionary.common.gui.screen.ElementScreen;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.common.util.RenderUtils;
import io.github.xienaoban.biologydictionary.mixin.rendering.GuiGraphicsIMixin;
import io.github.xienaoban.biologydictionary.mixin.rendering.GuiTextRenderStateIMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.List;
import java.util.Objects;

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
     * @see net.minecraft.client.renderer.GameRenderer#render(net.minecraft.client.DeltaTracker, boolean)
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
    public Font getFont()               { return screen.getFont(); }
    public float getZ()                 { return getCommonScreen().getZ(); }
    public boolean isDebug()            { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }

    public CommonScreen getCommonScreen()           { return (CommonScreen) screen; }
    public ElementScreen getElementScreen()           { return (ElementScreen) screen; }

    public Matrix3x2fStack getPose() {
        return getGuiGraphics().pose();
    }
    public GuiGraphics.ScissorStack getScissorStack() {
        return ((GuiGraphicsIMixin) getGuiGraphics()).biologydictionary$getScissorStack();
    }

    public GuiRenderState getGuiRenderState() {
        return ((GuiGraphicsIMixin) getGuiGraphics()).biologydictionary$getGuiRenderState();
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

    /**
     * getGuiGraphics().drawString(getFont(), component, (int) x, (int) y, color, false);
     *
     * @see net.minecraft.client.gui.GuiGraphics#drawString(net.minecraft.client.gui.Font, net.minecraft.network.chat.Component, int, int, int, boolean)
     * @see net.minecraft.client.gui.render.state.GuiTextRenderState#ensurePrepared()
     */
    public void renderText(Component component, int color, float z, float x, float y) {
        final class TextState extends GuiTextRenderState {
            public final float fx;
            public final float fy;

            public TextState(Font font, FormattedCharSequence text, Matrix3x2fc pose, float x, float y,
                             int color, int backgroundColor, boolean dropShadow, boolean includeEmpty,
                             ScreenRectangle screenRectangle) {
                super(font, text, pose, -1, -1, color, backgroundColor, dropShadow, includeEmpty, screenRectangle);
                this.fx = x;
                this.fy = y;
            }

            @Override
            public Font.PreparedText ensurePrepared() {
                GuiTextRenderStateIMixin self = (GuiTextRenderStateIMixin) (Object) this;
                if (self.biologydictionary$getPreparedText() == null) {
                    self.biologydictionary$setPreparedText(self.biologydictionary$getFont().prepareText(
                            self.biologydictionary$getText(), fx, fy, self.biologydictionary$getColor(),
                            self.biologydictionary$getDropShadow(), self.biologydictionary$getIncludeEmpty(),
                            self.biologydictionary$getBackgroundColor()));
                    ScreenRectangle screenRectangle = self.biologydictionary$getPreparedText().bounds();
                    if (screenRectangle != null) {
                        screenRectangle = screenRectangle.transformMaxBounds(this.pose);
                        self.biologydictionary$setBounds(self.biologydictionary$getScissor() != null ?
                                self.biologydictionary$getScissor().intersection(screenRectangle) : screenRectangle);
                    }
                }

                return self.biologydictionary$getPreparedText();
            }
        }
        
        if (ARGB.alpha(color) == 0) { return; }
        getGuiRenderState().submitText(new TextState(getFont(), component.getVisualOrderText(),
                new Matrix3x2f(getPose()), x, y, color, 0, false, false,
                getScissorStack().peek()));
    }

    public void renderText(Component component, int color, float size, float z, float x, float y) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderText(component, color, z, x / size, y / size);
        }
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
     * Another choice is:
     * {@snippet :
     *     getGuiGraphics().fill((int) left, (int) top, (int) right, (int) bottom, (int) z, color);
     * }
     * But it only supports `int`.
     *
     * @see net.minecraft.client.gui.GuiGraphics#submitColoredRectangle(RenderPipeline, TextureSetup, int, int, int, int, int, Integer)
     */
    public void renderRectangle(int color, float z, float left, float top, float right, float bottom) {
        record RectangleState(RenderPipeline pipeline, TextureSetup textureSetup,
                              Matrix3x2f pose, ScreenRectangle scissorArea, ScreenRectangle bounds,
                              int color,
                              float z, float x0, float y0, float x1, float y1) implements GuiElementRenderState {
            @Override
            public void buildVertices(VertexConsumer vertexConsumer) {
                vertexConsumer.addVertexWith2DPose(pose, x0, y0).setColor(color);
                vertexConsumer.addVertexWith2DPose(pose, x0, y1).setColor(color);
                vertexConsumer.addVertexWith2DPose(pose, x1, y1).setColor(color);
                vertexConsumer.addVertexWith2DPose(pose, x1, y0).setColor(color);
            }
        }
        Matrix3x2f pose = new Matrix3x2f(getPose());
        ScreenRectangle scissorArea = getScissorStack().peek();
        ScreenRectangle bounds = getBounds(left, top, right, bottom, pose, scissorArea);

        getGuiRenderState().submitGuiElement(new RectangleState(RenderPipelines.GUI, TextureSetup.noTexture(),
                pose, scissorArea, bounds, color, z, left, top, right, bottom));
    }

    public void renderTexture(TextureInfo texture,
                              float textureLeft, float textureTop,
                              float z, float left, float top, float width, float height) {
        renderTexture(texture,
                textureLeft, textureTop, textureLeft + width, textureTop + height,
                z, left, top, left + width,  top + height);
    }

    /**
     * {@snippet :
     *     getGuiGraphics().blit(RenderPipelines.GUI_TEXTURED, texture.location(),
     *         (int) left, (int) top,
     *         (int) textureLeft, (int) textureTop, (int) (textureRight - textureLeft), (int) (textureBottom - textureTop),
     *         (int) texture.width(), (int) texture.height());
     * }
     * @see net.minecraft.client.gui.GuiGraphics#submitBlit(com.mojang.blaze3d.pipeline.RenderPipeline, com.mojang.blaze3d.textures.GpuTextureView, com.mojang.blaze3d.textures.GpuSampler, int, int, int, int, float, float, float, float, int)
     */
    public void renderTexture(TextureInfo texture,
                              float textureLeft, float textureTop, float textureRight, float textureBottom,
                              float z, float left, float top, float right, float bottom) {
        record TextureState(RenderPipeline pipeline, TextureSetup textureSetup,
                            Matrix3x2f pose, ScreenRectangle scissorArea, ScreenRectangle bounds,
                            float u0, float v0, float u1, float v1,
                            float z, float x0, float y0, float x1, float y1) implements GuiElementRenderState {
            @Override
            public void buildVertices(VertexConsumer vertexConsumer) {
                vertexConsumer.addVertexWith2DPose(pose, x0, y0).setUv(u0, v0).setColor(-1);
                vertexConsumer.addVertexWith2DPose(pose, x0, y1).setUv(u0, v1).setColor(-1);
                vertexConsumer.addVertexWith2DPose(pose, x1, y1).setUv(u1, v1).setColor(-1);
                vertexConsumer.addVertexWith2DPose(pose, x1, y0).setUv(u1, v0).setColor(-1);
            }
        }
        AbstractTexture abstractTexture = getClient().getTextureManager().getTexture(texture.location());
        TextureSetup gpuTextureView = TextureSetup.singleTexture(abstractTexture.getTextureView(), abstractTexture.getSampler());
        float uvLeft = textureLeft / texture.width();
        float uvTop = textureTop / texture.height();
        float uvRight = textureRight / texture.width();
        float uvBottom = textureBottom / texture.height();
        Matrix3x2f pose = new Matrix3x2f(getPose());
        ScreenRectangle scissorArea = getScissorStack().peek();
        ScreenRectangle bounds = getBounds(left, top, right, bottom, pose, scissorArea);

        getGuiRenderState().submitGuiElement(new TextureState(RenderPipelines.GUI_TEXTURED, gpuTextureView,
                pose, scissorArea, bounds,
                uvLeft, uvTop, uvRight, uvBottom, z, left, top, right, bottom));
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
     * @see net.minecraft.client.gui.screens.inventory.EffectsInInventory#renderEffects(net.minecraft.client.gui.GuiGraphics, java.util.Collection, int, int, int, int, int)
     */
    public void renderEffect(Holder<MobEffect> effect, float left, float top) {
        Identifier id = Gui.getMobEffectSprite(effect);
        RenderPipeline renderPipeline = RenderPipelines.GUI_TEXTURED;
        getGuiGraphics().blitSprite(renderPipeline, id, (int) left, (int) top, 18, 18);
    }

    public void renderEffect(Holder<MobEffect> effect, float size, float left, float top) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderEffect(effect, left / size, top / size);
        }
    }

    //=======================================================================================
    // Rendering tooltips.
    //=======================================================================================

    public void renderComponentTooltipForNextFrameVanilla(List<Component> texts, float leftX, float topY) {
        getGuiGraphics().setComponentTooltipForNextFrame(getFont(), texts, (int) leftX, (int) topY);
    }

    public void renderComponentTooltipCenteredForNextFrameVanilla(List<Component> texts, float midX, float topY) {
        int maxLength = texts.stream().mapToInt(this::calcTextWidth).max().orElse(20);
        getGuiGraphics().setComponentTooltipForNextFrame(getFont(), texts,
                (int) (midX - (maxLength + 20) / 2F), (int) topY);
    }

    public void renderComponentTooltip(List<Component> texts, float leftX, float topY) {
        renderTooltip(texts, leftX, topY, 1F);
    }

    public void renderComponentTooltip(List<Component> texts, float size, float leftX, float topY) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderTooltip(texts, leftX, topY, size);
        }
    }

    public void renderComponentTooltipCentered(List<Component> texts, float midX, float topY) {
        int maxLength = texts.stream().mapToInt(this::calcTextWidth).max().orElse(20);
        renderComponentTooltip(texts, midX - (maxLength + 6) / 2F, topY);
    }

    public void renderComponentTooltipCentered(List<Component> texts, float size, float midX, float topY) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            int maxLength = texts.stream().mapToInt(this::calcTextWidth).max().orElse(20);
            renderTooltip(texts, midX - (maxLength + 6) * size / 2F, topY, size);
        }
    }

    /**
     * Similar to GuiGraphics#renderTooltip.
     * The only difference is that we give the argument {@code size}
     * to calculate the real gui width and gui height.
     *
     * @see net.minecraft.client.gui.GuiGraphics#renderTooltip(net.minecraft.client.gui.Font, java.util.List, int, int, net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner, net.minecraft.resources.Identifier)
     */
    private void renderTooltip(List<Component> texts,
                               float x, float y, float size
    ) {
        Font font = getFont();
        List<ClientTooltipComponent> list = texts.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList();
        ClientTooltipPositioner clientTooltipPositioner = DefaultTooltipPositioner.INSTANCE;

        int width = 0;
        int height = list.size() == 1 ? -2 : 0;

        for (ClientTooltipComponent clientTooltipComponent : list) {
            int m = clientTooltipComponent.getWidth(font);
            if (m > width) {
                width = m;
            }
            height += clientTooltipComponent.getHeight(font);
        }

        Vector2ic vector2ic = clientTooltipPositioner.positionTooltip((int) (getGuiGraphics().guiWidth() / size), (int) (getGuiGraphics().guiHeight() / size), (int) (x / size - 8), (int) (y / size + 16), width, height);
        int p = vector2ic.x();
        int q = vector2ic.y();
        getPose().pushMatrix();
        TooltipRenderUtil.renderTooltipBackground(getGuiGraphics(), p, q, width, height, null);
        int r = q;

        for (int s = 0; s < list.size(); s++) {
            ClientTooltipComponent clientTooltipComponent2 = list.get(s);
            clientTooltipComponent2.renderText(getGuiGraphics(), font, p, r);
            r += clientTooltipComponent2.getHeight(font) + (s == 0 ? 2 : 0);
        }

        r = q;

        for (int s = 0; s < list.size(); s++) {
            ClientTooltipComponent clientTooltipComponent2 = list.get(s);
            clientTooltipComponent2.renderImage(font, p, r, width, height, getGuiGraphics());
            r += clientTooltipComponent2.getHeight(font) + (s == 0 ? 2 : 0);
        }

        getPose().popMatrix();
    }

    //=======================================================================================
    // Rendering entities.
    //=======================================================================================

    public void renderEntityBottomed(Entity entity, @Nullable ScreenRenderingContext.EntityRenderingCache cache,
                                     float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, float forceScale) {
        renderEntity(entity, cache, left, top, right, bottom, rotateX, rotateY, forceScale, 1F);
    }

    public void renderEntityCentered(Entity entity, @Nullable ScreenRenderingContext.EntityRenderingCache cache,
                                     float left, float top, float right, float bottom,
                                     float rotateX, float rotateY) {
        renderEntity(entity, cache, left, top, right, bottom, rotateX, rotateY, -1, 1.9F);
    }

    public void renderEntityCentered(Entity entity, @Nullable ScreenRenderingContext.EntityRenderingCache cache,
                                     float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, float forceScale) {
        renderEntity(entity, cache, left, top, right, bottom, rotateX, rotateY, forceScale, 1.9F);
    }

    /**
     * {@snippet :
     *     InventoryScreen.renderEntityInInventoryFollowsMouse(getGuiGraphics(), (int) left, (int) top, (int) right, (int) bottom, 30, 0.0625F, rotateX * 20, rotateY * 20, livingEntity);
     * }
     *
     * @see net.minecraft.client.gui.screens.inventory.InventoryScreen#renderEntityInInventoryFollowsMouse(net.minecraft.client.gui.GuiGraphics, int, int, int, int, int, float, float, float, net.minecraft.world.entity.LivingEntity)
     */
    private void renderEntity(Entity entity, @Nullable ScreenRenderingContext.EntityRenderingCache cache, float left, float top, float right, float bottom,
                              float rotateX, float rotateY, float forceScale, float internalOffset) {
        // This function does not compatible with guiGraphics.pose().scale(size, size).
        if (screenScale != 1F) {
            left   *= screenScale;
            top    *= screenScale;
            right  *= screenScale;
            bottom *= screenScale;
        }

        final float width = right - left;
        final float height = bottom - top;
        final float entityWidth = entity.getBbWidth();
        final float entityHeight = entity.getBbHeight();

        boolean cached = (cache != null && cache.cached)
                && (cache.width == width && cache.height == height)
                && (cache.entityWidth == entityWidth && cache.entityHeight == entityHeight);

        final float scale;
        if (cached) {
            scale = cache.scale;
        } else {
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
        }

        int x0 = Mth.ceil(left), y0 = Mth.ceil(top), x1 = Mth.floor(right), y1 = Mth.floor(bottom);
        Quaternionf quaternionf = new Quaternionf().rotateX(rotateY * 20F * (float) (Math.PI / 180F));
        Quaternionf quaternionf2 = new Quaternionf().rotateY((float) Math.PI - rotateX * 20F * (float) (Math.PI / 180F));
        Quaternionf quaternionf3 = new Quaternionf().rotateZ((float) Math.PI);
        quaternionf.mul(quaternionf2).mul(quaternionf3);
        float sc = entity instanceof LivingEntity living ? living.getScale() : 1F;
        Vector3f vector3f = new Vector3f(0F, entity.getBbHeight() / internalOffset + 0.0625F * sc, 0F);

        EntityRenderDispatcher entityRenderDispatcher = getClient().getEntityRenderDispatcher();
        EntityRenderer<Entity, EntityRenderState> entityRenderer = RenderUtils.getRenderer(entityRenderDispatcher, entity);
        EntityRenderState entityRenderState;
        if (cache != null && cache.entityRenderState != null) {
            entityRenderState = cache.entityRenderState;
        } else {
            entityRenderState = RenderUtils.createRenderState(entityRenderer);
        }
        entityRenderer.extractRenderState(entity, entityRenderState, 1F);
        entityRenderState.lightCoords = 15728880;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        getGuiGraphics().submitEntityRenderState(entityRenderState, scale / sc, vector3f, quaternionf, null, x0, y0, x1, y1);

        if (isDebug() && width > 0 && height > 0) {
            final int color = 0xFFAAAAAA;
            renderRectangle(color, 0.6F, getZ(), left, top, right, bottom);
        }

        if (cache != null && !cached) {
            cache.cached = true;

            cache.width = width;
            cache.height = height;
            cache.entityWidth = entityWidth;
            cache.entityHeight = entityHeight;

            cache.entityRenderState = entityRenderState;
            cache.scale = scale;
        }
    }

    public void renderPlayerFace(AbstractClientPlayer player, float left, float top) {
        renderPlayerFace(player, left, top, 8F);
    }

    /**
     * @see net.minecraft.client.gui.components.PlayerTabOverlay#render(net.minecraft.client.gui.GuiGraphics, int, net.minecraft.world.scores.Scoreboard, net.minecraft.world.scores.Objective)
     */
    public void renderPlayerFace(AbstractClientPlayer player, float left, float top, float size) {
        boolean ud = AvatarRenderer.isPlayerUpsideDown(player);
        PlayerFaceRenderer.draw(getGuiGraphics(), player.getSkin().body().texturePath(), (int) left, (int) top, (int) size, /* show hat */ true, ud, -1);
    }

    private static ScreenRectangle getBounds(float x0, float y0, float x1, float y1, Matrix3x2f pose, @Nullable ScreenRectangle screenRectangle) {
        int x0i = Mth.floor(x0);    int x1i = Mth.ceil(x1);
        int y0i = Mth.floor(y0);    int y1i = Mth.ceil(y1);
        ScreenRectangle screenRectangle2 = new ScreenRectangle(x0i, y0i, x1i - x0i, y1i - y0i).transformMaxBounds(pose);
        return screenRectangle != null ? screenRectangle.intersection(screenRectangle2) : screenRectangle2;
    }

    public static final class EntityRenderingCache {
        private boolean cached;

        private float width;
        private float height;
        private float entityWidth;
        private float entityHeight;

        private EntityRenderState entityRenderState;
        private float scale;
    }
}
