package io.github.xienaoban.biologydictionary.platform.gui.screen.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.mixin.rendering.GuiGraphicsExtractorIMixin;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.TextureInfo;
import io.github.xienaoban.biologydictionary.platform.gui.screen.CommonScreen;
import io.github.xienaoban.biologydictionary.platform.gui.screen.ElementScreen;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@ClientOnly
public final class ScreenRenderingContext {
    private static final int TOOLTIP_WIDTH = 240;
    private static final float INVENTORY_ENTITY_ROTATION_DEGREES = 20.0F;

    private final Screen screen;
    private final Minecraft client;

    private GuiGraphicsExtractor guiGraphics;
    private Font font;
    private float z;
    private float screenScale;
    private float reciprocalScreenScale;
    private int rawScreenWidth;
    private int rawScreenHeight;
    private int screenWidth;
    private int screenHeight;
    private float rawMouseX;
    private float rawMouseY;
    private float mouseX;
    private float mouseY;
    private float tickDelta;

    public ScreenRenderingContext(Screen screen) {
        client = Objects.requireNonNull(ClientUtils.getClient());
        this.screen = screen;
        update(client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight(), screen.getFont(), 0F, 1F);
    }

    /**
     * Used in init().
     */
    public void update(int width, int height, Font font, float z, float screenScale) {
        this.screenScale = screenScale;
        this.reciprocalScreenScale = 1F / screenScale;
        this.font = font;
        this.z = z;

        this.rawScreenWidth = width;
        this.rawScreenHeight = height;
        this.screenWidth = calcScaledValue(rawScreenWidth);
        this.screenHeight = calcScaledValue(rawScreenHeight);
    }

    /**
     * Used in render().
     * We don't use the mouseX and mouseY parameters because they are int.
     * @see net.minecraft.client.renderer.GameRenderer#render(net.minecraft.client.DeltaTracker, boolean)
     *
     * @param mouseX not used
     * @param mouseY not used
     */
    public void update(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta) {
        this.guiGraphics = guiGraphics;
        this.tickDelta = tickDelta;
        this.rawMouseX = (float) client.mouseHandler.xpos() * (float) client.getWindow().getGuiScaledWidth()
                / (float) client.getWindow().getScreenWidth();
        this.rawMouseY = (float) client.mouseHandler.ypos() * (float) client.getWindow().getGuiScaledHeight()
                / (float) client.getWindow().getScreenHeight();
        assert mouseX == (int) this.rawMouseX && mouseY == (int) this.rawMouseY;

        this.mouseX = calcScaledValue(this.rawMouseX);
        this.mouseY = calcScaledValue(this.rawMouseY);
    }

    public Minecraft getClient() { return client; }
    public Screen getScreen() { return screen; }
    public GuiGraphicsExtractor getGuiGraphics() { return guiGraphics; }
    public int getRawScreenWidth() { return rawScreenWidth; }
    public int getRawScreenHeight() { return rawScreenHeight; }
    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }
    public float getRawMouseX() { return rawMouseX; }
    public float getRawMouseY() { return rawMouseY; }
    public float getMouseX() { return mouseX; }
    public float getMouseY() { return mouseY; }
    public float getTickDelta() { return tickDelta; }
    public Font getFont() { return font; }
    public float getZ() { return z; }
    public float getScreenScale() { return screenScale; }
    public float getReciprocalScreenScale() { return reciprocalScreenScale; }
    public CommonScreen getCommonScreen() { return (CommonScreen) screen; }
    public ElementScreen getElementScreen() { return (ElementScreen) screen; }

    public Matrix3x2fStack getPose() {
        return getGuiGraphics().pose();
    }

    public void nextStratum() {
        getGuiGraphics().nextStratum();
    }

    public int calcScaledValue(int value) {
        return Mth.ceil(value * reciprocalScreenScale);
    }

    public float calcScaledValue(float value) {
        return value * reciprocalScreenScale;
    }

    public ScaleRAII scaleOnce(float size) {
        return new ScaleRAII(this, size);
    }

    public ScaleRAII scaleOnce(float size, float z) {
        return new ScaleRAII(this, size, z);
    }

    public ScaleRAII scaleToOriginalOnce() {
        return scaleOnce(reciprocalScreenScale);
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

    public void renderText(FormattedCharSequence text, int color, float z, float x, float y) {
        float ix = (float) Math.floor(x);
        float iy = (float) Math.floor(y);
        float dx = x - ix;
        float dy = y - iy;

        Matrix3x2fStack pose = getPose();
        pose.pushMatrix();
        try {
            pose.translate(dx, dy);
            getGuiGraphics().text(getFont(), text, (int) ix, (int) iy, color, false);
        } finally {
            pose.popMatrix();
        }
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
     * Another choice is:
     * {@snippet :
     *     getGuiGraphics().fill((int) left, (int) top, (int) right, (int) bottom, (int) z, color);
     * }
     * But it only supports `int`.
     */
    public void renderRectangle(int color, float z, float left, float top, float right, float bottom) {
        record RectangleState(RenderPipeline pipeline, TextureSetup textureSetup,
                              Matrix3x2f pose, ScreenRectangle scissorArea, ScreenRectangle bounds,
                              int color, float x0, float y0, float x1, float y1) implements GuiElementRenderState {
            @Override
            public void buildVertices(VertexConsumer vertexConsumer) {
                vertexConsumer.addVertexWith2DPose(pose, x0, y0).setColor(color);
                vertexConsumer.addVertexWith2DPose(pose, x0, y1).setColor(color);
                vertexConsumer.addVertexWith2DPose(pose, x1, y1).setColor(color);
                vertexConsumer.addVertexWith2DPose(pose, x1, y0).setColor(color);
            }
        }

        Matrix3x2f pose = new Matrix3x2f(getPose());
        GuiGraphicsExtractorIMixin guiGraphicsAccessor = (GuiGraphicsExtractorIMixin) guiGraphics;
        ScreenRectangle scissorArea = guiGraphicsAccessor.biologydictionary$getScissorStack().peek();
        ScreenRectangle bounds = getBounds(left, top, right, bottom, pose, scissorArea);
        guiGraphicsAccessor.biologydictionary$getGuiRenderState().addGuiElement(new RectangleState(
                RenderPipelines.GUI, TextureSetup.noTexture(),
                pose, scissorArea, bounds, color, left, top, right, bottom));
    }

    public void renderTexture(TextureInfo texture, float left, float top) {
        renderTexture(texture, left, top, texture.width(), texture.height());
    }

    public void renderTexture(TextureInfo texture, float left, float top, float width, float height) {
        renderTexture(texture, RenderPipelines.GUI_TEXTURED, left, top, width, height);
    }

    public void renderTexture(TextureInfo texture, RenderPipeline pipeline,
                              float left, float top, float width, float height) {
        renderTexture(texture, pipeline, 0.0F, 0.0F, texture.width(), texture.height(),
                left, top, left + width, top + height);
    }

    public void renderTexture(TextureInfo texture,
                              float textureLeft, float textureTop,
                              float z, float left, float top, float width, float height) {
        renderTexture(texture, textureLeft, textureTop, textureLeft + width, textureTop + height,
                z, left, top, left + width, top + height);
    }

    public void renderTexture(TextureInfo texture,
                              float textureLeft, float textureTop, float textureRight, float textureBottom,
                              float z, float left, float top, float right, float bottom) {
        renderTexture(texture, RenderPipelines.GUI_TEXTURED, textureLeft, textureTop, textureRight, textureBottom,
                left, top, right, bottom);
    }

    public void renderTexture(TextureInfo texture, float left, float top, float u0, float v0, float u1, float v1) {
        renderTexture(texture, RenderPipelines.GUI_TEXTURED, u0, v0, u1, v1,
                left, top, left + u1 - u0, top + v1 - v0);
    }

    private void renderTexture(TextureInfo texture, RenderPipeline pipeline,
                               float textureLeft, float textureTop, float textureRight, float textureBottom,
                               float left, float top, float right, float bottom) {
        record TextureState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose,
                            ScreenRectangle scissorArea, ScreenRectangle bounds,
                            float u0, float v0, float u1, float v1,
                            float x0, float y0, float x1, float y1) implements GuiElementRenderState {
            @Override
            public void buildVertices(VertexConsumer vertexConsumer) {
                vertexConsumer.addVertexWith2DPose(pose, x0, y0).setUv(u0, v0).setColor(-1);
                vertexConsumer.addVertexWith2DPose(pose, x0, y1).setUv(u0, v1).setColor(-1);
                vertexConsumer.addVertexWith2DPose(pose, x1, y1).setUv(u1, v1).setColor(-1);
                vertexConsumer.addVertexWith2DPose(pose, x1, y0).setUv(u1, v0).setColor(-1);
            }
        }

        AbstractTexture abstractTexture = client.getTextureManager().getTexture(texture.location());
        TextureSetup textureSetup = TextureSetup.singleTexture(
                abstractTexture.getTextureView(), abstractTexture.getSampler());
        float u0 = textureLeft / texture.width();
        float v0 = textureTop / texture.height();
        float u1 = textureRight / texture.width();
        float v1 = textureBottom / texture.height();
        Matrix3x2f pose = new Matrix3x2f(getPose());
        GuiGraphicsExtractorIMixin guiGraphicsAccessor = (GuiGraphicsExtractorIMixin) guiGraphics;
        ScreenRectangle scissorArea = guiGraphicsAccessor.biologydictionary$getScissorStack().peek();
        ScreenRectangle bounds = getBounds(left, top, right, bottom, pose, scissorArea);
        guiGraphicsAccessor.biologydictionary$getGuiRenderState().addGuiElement(new TextureState(
                pipeline, textureSetup, pose, scissorArea, bounds,
                u0, v0, u1, v1, left, top, right, bottom));
    }

    public void renderItem(ItemStack itemStack, float left, float top) {
        getGuiGraphics().item(itemStack, (int) left, (int) top);
    }

    public void renderItem(ItemStack itemStack, float size, float left, float top) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderItem(itemStack, left / size, top / size);
        }
    }

    public void renderEffect(Holder<MobEffect> effect, float left, float top) {
        Identifier id = Gui.getMobEffectSprite(effect);
        getGuiGraphics().blitSprite(RenderPipelines.GUI_TEXTURED, id, (int) left, (int) top, 18, 18);
    }

    public void renderEffect(Holder<MobEffect> effect, float size, float left, float top) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderEffect(effect, left / size, top / size);
        }
    }

    public void renderComponentTooltip(List<Component> texts, float leftX, float topY) {
        renderTooltipTexts(texts, 1.0F, leftX, topY);
    }

    public void renderComponentTooltip(List<Component> texts, float size, float leftX, float topY) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderTooltipTexts(texts, size, leftX, topY);
        }
    }

    public void renderComponentTooltipCentered(List<Component> texts, float midX, float topY) {
        List<FormattedCharSequence> lines = splitTooltipTexts(texts);
        renderTooltipLines(lines, 1.0F, midX - (calcMaxTextWidth(lines) + 6) / 2.0F, topY);
    }

    public void renderComponentTooltipCentered(List<Component> texts, float size, float midX, float topY) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            List<FormattedCharSequence> lines = splitTooltipTexts(texts);
            renderTooltipLines(lines, size, midX - (calcMaxTextWidth(lines) + 6) * size / 2.0F, topY);
        }
    }

    public void renderLinedTooltip(List<FormattedCharSequence> lines, float leftX, float topY) {
        renderTooltipLines(lines, 1.0F, leftX, topY);
    }

    public void renderLinedTooltip(List<FormattedCharSequence> lines, float size, float leftX, float topY) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderTooltipLines(lines, size, leftX, topY);
        }
    }

    public void renderLinedTooltipCentered(List<FormattedCharSequence> lines, float midX, float topY) {
        renderTooltipLines(lines, 1.0F, midX - (calcMaxTextWidth(lines) + 6) / 2.0F, topY);
    }

    public void renderLinedTooltipCentered(List<FormattedCharSequence> lines, float size, float midX, float topY) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderTooltipLines(lines, size, midX - (calcMaxTextWidth(lines) + 6) * size / 2.0F, topY);
        }
    }

    private List<FormattedCharSequence> splitTooltipTexts(List<Component> texts) {
        return texts.stream()
                .flatMap(component -> {
                    List<FormattedCharSequence> lines = getFont().split(component, TOOLTIP_WIDTH);
                    return lines.isEmpty() ? Stream.of(Component.empty().getVisualOrderText()) : lines.stream();
                })
                .toList();
    }

    private int calcMaxTextWidth(List<FormattedCharSequence> lines) {
        return lines.stream().mapToInt(this::calcTextWidth).max().orElse(20);
    }

    private void renderTooltipTexts(List<Component> texts, float size, float x, float y) {
        renderTooltipLines(splitTooltipTexts(texts), size, x, y);
    }

    private void renderTooltipLines(List<FormattedCharSequence> lines, float size, float x, float y) {
        if (lines.isEmpty()) {
            return;
        }

        Font font = getFont();
        List<ClientTooltipComponent> components = lines.stream()
                .map(ClientTooltipComponent::create)
                .toList();

        ClientTooltipPositioner positioner = (rawScreenWidth, rawScreenHeight, mouseX, mouseY, tooltipWidth, tooltipHeight) ->
                DefaultTooltipPositioner.INSTANCE.positionTooltip(
                        (int) (getScreenWidth() / size), (int) (getScreenHeight() / size),
                        mouseX, mouseY, tooltipWidth, tooltipHeight);
        getGuiGraphics().tooltip(font, components, (int) (x / size - 8.0F), (int) (y / size + 16.0F),
                positioner, Textures.BOOK_TOOLTIP);
    }

    public void renderEntityBottomed(Entity entity, EntityRenderingCache cache,
                                     float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, float forceScale) {
        renderEntity(entity, cache, left, top, right, bottom, rotateX, rotateY, forceScale, 1.0F);
    }

    public void renderEntityCentered(Entity entity, EntityRenderingCache cache,
                                     float left, float top, float right, float bottom,
                                     float rotateX, float rotateY) {
        renderEntity(entity, cache, left, top, right, bottom, rotateX, rotateY, -1.0F, 1.9F);
    }

    public void renderEntityCentered(Entity entity, EntityRenderingCache cache,
                                     float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, float forceScale) {
        renderEntity(entity, cache, left, top, right, bottom, rotateX, rotateY, forceScale, 1.9F);
    }

    public void renderEntityCentered(Entity entity, EntityRenderingCache cache,
                                     float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, int silhouetteColor) {
        renderEntity(entity, cache, left, top, right, bottom, rotateX, rotateY, -1.0F, 1.9F, silhouetteColor);
    }

    public void renderEntityCentered(Entity entity, EntityRenderingCache cache,
                                     float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, float forceScale, int silhouetteColor) {
        renderEntity(entity, cache, left, top, right, bottom, rotateX, rotateY, forceScale, 1.9F, silhouetteColor);
    }

    private void renderEntity(Entity entity, EntityRenderingCache cache, float left, float top, float right,
                              float bottom, float rotateX, float rotateY, float forceScale, float internalOffset) {
        renderEntity(entity, cache, left, top, right, bottom, rotateX, rotateY, forceScale, internalOffset, 0);
    }

    /**
     * Mirrors the vanilla inventory pose setup, but keeps the generic renderer path so non-living entities work too.
     *
     * @see net.minecraft.client.gui.screens.inventory.InventoryScreen#extractEntityInInventoryFollowsMouse
     */
    private void renderEntity(Entity entity, EntityRenderingCache cache, float left, float top, float right,
                              float bottom, float rotateX, float rotateY, float forceScale, float internalOffset,
                              int silhouetteColor) {
        if (screenScale != 1F) {
            left *= screenScale;
            top *= screenScale;
            right *= screenScale;
            bottom *= screenScale;
        }

        float width = right - left;
        float height = bottom - top;
        float entityWidth = entity.getBbWidth();
        float entityHeight = entity.getBbHeight();
        boolean cached = cache != null
                && cache.cached
                && cache.width == width
                && cache.height == height
                && cache.entityWidth == entityWidth
                && cache.entityHeight == entityHeight;

        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
        EntityRenderer<Entity, EntityRenderState> renderer = RenderUtils.getRenderer(dispatcher, entity);
        EntityRenderState renderState = cache != null && cache.renderState != null
                ? cache.renderState
                : RenderUtils.createRenderState(renderer);
        RenderUtils.extractRenderState(renderer, entity, renderState, tickDelta);
        RenderUtils.renderBodyOnly(renderState);
        renderState.outlineColor = silhouetteColor;

        float xAngleDegrees = rotateX * INVENTORY_ENTITY_ROTATION_DEGREES;
        float yAngleDegrees = rotateY * INVENTORY_ENTITY_ROTATION_DEGREES;
        Quaternionf rotation = new Quaternionf().rotateX(yAngleDegrees * Mth.DEG_TO_RAD);
        rotation.mul(new Quaternionf().rotateY((float) Math.PI - xAngleDegrees * Mth.DEG_TO_RAD));
        rotation.mul(new Quaternionf().rotateZ((float) Math.PI));

        if (renderState instanceof LivingEntityRenderState livingState) {
            livingState.bodyRot = 0.0F;
            livingState.yRot = 0.0F;
            livingState.xRot = 0.0F;
        }

        float scale;
        if (cached) {
            scale = cache.scale;
        } else if (forceScale < 0.0F) {
            float vw = entityWidth > 1.8F ? entityWidth / 1.4F : (float) Math.log(1.1F + entityWidth);
            float vh = entityHeight > 2.2F ? entityHeight / 1.8F : (float) Math.log(1.1F + entityHeight);
            scale = Math.min(width / vw, height / vh) / 2.2F;
        } else {
            float safeEntityWidth = Math.max(entityWidth, 0.1F);
            float safeEntityHeight = Math.max(entityHeight, 0.1F);
            scale = Math.min(width / safeEntityWidth, height / safeEntityHeight) / 1.5F * forceScale;
        }

        Vector3f translation = new Vector3f(0.0F, renderState.boundingBoxHeight / internalOffset + 0.0625F, 0.0F);
        getGuiGraphics().entity(renderState, scale, translation, rotation, null,
                Mth.ceil(left), Mth.ceil(top), Mth.floor(right), Mth.floor(bottom));

        if (BiologyDictionaryClient.isDebugMode() && width > 0.0F && height > 0.0F) {
            renderRectangle(0xFFAAAAAA, 0.6F, getZ(),
                    left / screenScale, top / screenScale, right / screenScale, bottom / screenScale);
        }

        if (cache != null && !cached) {
            cache.cached = true;
            cache.width = width;
            cache.height = height;
            cache.entityWidth = entityWidth;
            cache.entityHeight = entityHeight;
            cache.renderState = renderState;
            cache.scale = scale;
        }
    }

    public void renderPlayerFace(AbstractClientPlayer player, float left, float top) {
        renderPlayerFace(player, left, top, 8);
    }

    public void renderPlayerFace(AbstractClientPlayer player, float left, float top, float size) {
        PlayerFaceExtractor.extractRenderState(getGuiGraphics(), player.getSkin().body().texturePath(),
                (int) left, (int) top, (int) size,
                player.isModelPartShown(PlayerModelPart.HAT), AvatarRenderer.isPlayerUpsideDown(player), -1);
    }

    public static final class EntityRenderingCache {
        private boolean cached;
        private float width;
        private float height;
        private float entityWidth;
        private float entityHeight;
        private EntityRenderState renderState;
        private float scale;

        public EntityRenderingCache() {
            this.renderState = null;
        }

        public EntityRenderingCache(EntityRenderDispatcher renderDispatcher, Entity entity) {
            this.renderState = RenderUtils.createRenderState(renderDispatcher, entity);
        }
    }

    private static ScreenRectangle getBounds(float x0, float y0, float x1, float y1,
                                             Matrix3x2f pose, ScreenRectangle scissorArea) {
        int x0i = Mth.floor(x0);
        int y0i = Mth.floor(y0);
        int x1i = Mth.ceil(x1);
        int y1i = Mth.ceil(y1);
        ScreenRectangle bounds = new ScreenRectangle(x0i, y0i, x1i - x0i, y1i - y0i).transformMaxBounds(pose);
        return scissorArea == null ? bounds : scissorArea.intersection(bounds);
    }
}
