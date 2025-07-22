package io.github.xienaoban.biologydictionary.common.gui.screen.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.xienaoban.biologydictionary.common.gui.TextureInfo;
import io.github.xienaoban.biologydictionary.common.gui.screen.CommonScreen;
import io.github.xienaoban.biologydictionary.common.gui.screen.ElementScreen;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class ScreenRenderingContext {
    private final Screen screen;

    Minecraft client;
    GuiGraphics guiGraphics;
    private float mouseX, mouseY;
    private float tickDelta;
    private boolean debug;

    public ScreenRenderingContext(Screen screen) {
        this.client = Objects.requireNonNull(McClientUtils.getClient());
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

        this.mouseX = (float) client.mouseHandler.xpos() * (float) client.getWindow().getGuiScaledWidth() / (float) client.getWindow().getScreenWidth();
        this.mouseY = (float) client.mouseHandler.ypos() * (float) client.getWindow().getGuiScaledHeight() / (float) client.getWindow().getScreenHeight();
        assert mouseX == (int) this.mouseX && mouseY == (int) this.mouseY;
    }

    public Minecraft getClient()     { return client; }
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
    public GuiGraphics.ScissorStack getScissorStack() { return getGuiGraphics().scissorStack; }
    public GuiSpriteManager getSpriteManager() { return client.getGuiSprites(); }
    public GuiRenderState getGuiRenderState() { return getGuiGraphics().guiRenderState; }

    public MultiBufferSource.BufferSource getBufferSource() {
        return null;
    }

    public ScaleRAII scaleOnce(float size) {
        return new ScaleRAII(this, size);
    }

    public ScaleRAII scaleOnce(float size, float z) {
        return new ScaleRAII(this, size, z);
    }

    public int calcTextWidth(Component component) {
        return getFont().width(component);
    }

    public void renderText(Component component, int color, float z, float x, float y) {
        if (z != getZ()) {
            try (ScaleRAII ignored = scaleOnce(1F, z)) {
                getGuiGraphics().drawString(getFont(), component, (int) x, (int) y, color, false);
            }
        } else {
            getGuiGraphics().drawString(getFont(), component, (int) x, (int) y, color, false);
        }
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
            public void buildVertices(VertexConsumer vertexConsumer, float f) {
                vertexConsumer.addVertexWith2DPose(pose, x0, y0, z + f).setColor(color);
                vertexConsumer.addVertexWith2DPose(pose, x0, y1, z + f).setColor(color);
                vertexConsumer.addVertexWith2DPose(pose, x1, y1, z + f).setColor(color);
                vertexConsumer.addVertexWith2DPose(pose, x1, y0, z + f).setColor(color);
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
     * @see net.minecraft.client.gui.GuiGraphics#submitBlit(RenderPipeline, GpuTextureView, int, int, int, int, float, float, float, float, int)
     */
    public void renderTexture(TextureInfo texture,
                              float textureLeft, float textureTop, float textureRight, float textureBottom,
                              float z, float left, float top, float right, float bottom) {
        record TextureState(RenderPipeline pipeline, TextureSetup textureSetup,
                            Matrix3x2f pose, ScreenRectangle scissorArea, ScreenRectangle bounds,
                            float u0, float v0, float u1, float v1,
                            float z, float x0, float y0, float x1, float y1) implements GuiElementRenderState {
            @Override
            public void buildVertices(VertexConsumer vertexConsumer, float f) {
                vertexConsumer.addVertexWith2DPose(pose, x0, y0, z + f).setUv(u0, v0).setColor(-1);
                vertexConsumer.addVertexWith2DPose(pose, x0, y1, z + f).setUv(u0, v1).setColor(-1);
                vertexConsumer.addVertexWith2DPose(pose, x1, y1, z + f).setUv(u1, v1).setColor(-1);
                vertexConsumer.addVertexWith2DPose(pose, x1, y0, z + f).setUv(u1, v0).setColor(-1);
            }
        }

        TextureSetup gpuTextureView = TextureSetup.singleTexture(getClient().getTextureManager().getTexture(texture.location()).getTextureView());
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

    public void renderItem(ItemStack itemStack, float left, float top) {
        getGuiGraphics().renderFakeItem(itemStack, (int) left, (int) top);
    }

    public void renderItem(ItemStack itemStack, float size, float left, float top) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderItem(itemStack, left / size, top / size);
        }
    }

    /**
     * @see net.minecraft.client.gui.screens.inventory.EffectsInInventory#renderEffects(GuiGraphics, int, int)
     * @see net.minecraft.client.gui.screens.inventory.EffectsInInventory#renderIcons(net.minecraft.client.gui.GuiGraphics, int, int, java.lang.Iterable, boolean)
     */
    public void renderSprite(Holder<MobEffect> effect, float left, float top) {
        ResourceLocation resourceLocation = Gui.getMobEffectSprite(effect);
        TextureAtlasSprite textureAtlasSprite = getSpriteManager().getSprite(resourceLocation);
        GuiSpriteScaling guiSpriteScaling = getSpriteManager().getSpriteScaling(textureAtlasSprite);
        RenderPipeline renderPipeline = RenderPipelines.GUI_TEXTURED;
        getGuiGraphics().blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, (int) left, (int) top, 18, 18);
    }

    public void renderSprite(Holder<MobEffect> effect, float size, float left, float top) {
        try (ScaleRAII ignored = scaleOnce(size)) {
            renderSprite(effect, left / size, top / size);
        }
    }

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

    /**
     * {@snippet :
     *     InventoryScreen.renderEntityInInventoryFollowsMouse(getGuiGraphics(), (int) left, (int) top, (int) right, (int) bottom, 30, 0.0625F, rotateX * 20, rotateY * 20, livingEntity);
     * }
     *
     * @see net.minecraft.client.gui.screens.inventory.InventoryScreen#renderEntityInInventoryFollowsMouse(net.minecraft.client.gui.GuiGraphics, int, int, int, int, int, float, float, float, net.minecraft.world.entity.LivingEntity)
     * @see net.minecraft.client.gui.screens.inventory.InventoryScreen#renderEntityInInventory(GuiGraphics, int, int, int, int, float, Vector3f, Quaternionf, Quaternionf, LivingEntity)
     */
    private void renderEntity(Entity entity, float left, float top, float right, float bottom,
                              float rotateX, float rotateY, float forceScale, float internalOffset) {
        final float width = right - left;
        final float height = bottom - top;
        final float entityWidth = entity.getBbWidth();
        final float entityHeight = entity.getBbHeight();
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

        int x0 = Mth.ceil(left), y0 = Mth.ceil(top), x1 = Mth.floor(right), y1 = Mth.floor(bottom);
        getGuiGraphics().enableScissor(x0, y0, x1, y1);
        Quaternionf quaternionf = new Quaternionf().rotateX(rotateY * 20F * (float) (Math.PI / 180F));
        Quaternionf quaternionf2 = new Quaternionf().rotateY((float) Math.PI - rotateX * 20F * (float) (Math.PI / 180F));
        Quaternionf quaternionf3 = new Quaternionf().rotateZ((float) Math.PI);
        quaternionf.mul(quaternionf2).mul(quaternionf3);
        float sc = entity instanceof LivingEntity living ? living.getScale() : 1F;
        Vector3f vector3f = new Vector3f(0F, entity.getBbHeight() / internalOffset + 0.0625F * sc, 0F);

        EntityRenderDispatcher entityRenderDispatcher = getClient().getEntityRenderDispatcher();
        EntityRenderer<Entity, EntityRenderState> entityRenderer = Misc.cast(entityRenderDispatcher.getRenderer(entity));
        EntityRenderState entityRenderState = entityRenderer.createRenderState();
        entityRenderer.extractRenderState(entity, entityRenderState, 1F);
        getGuiGraphics().submitEntityRenderState(entityRenderState, scale / sc, vector3f, quaternionf, null, x0, y0, x1, y1);

        getGuiGraphics().disableScissor();

        if (isDebug() && width > 0 && height > 0) {
            final int color = 0xFFAAAAAA;
            renderRectangle(color, 0.6F, getZ(), left, top, right, bottom);
        }
    }

    /**
     * @see net.minecraft.client.gui.screens.inventory.PageButton#playDownSound(net.minecraft.client.sounds.SoundManager)
     */
    public void playScreenSound(SoundEvent sound, float volume, float pitch) {
        getClient().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    private static ScreenRectangle getBounds(float x0, float y0, float x1, float y1, Matrix3x2f matrix3x2f, @Nullable ScreenRectangle screenRectangle) {
        int x0i = Mth.floor(x0);    int x1i = Mth.ceil(x1);
        int y0i = Mth.floor(y0);    int y1i = Mth.ceil(y1);
        ScreenRectangle screenRectangle2 = new ScreenRectangle(x0i, y0i, x1i - x0i, y1i - y0i).transformMaxBounds(matrix3x2f);
        return screenRectangle != null ? screenRectangle.intersection(screenRectangle2) : screenRectangle2;
    }

    private static float sigmoid(float x) {
        return (float) (1F / (1F + Math.exp(-x)));
    }

    private static float zero2one(float x) {
        return sigmoid(x) * 2 - 1;
    }
}
