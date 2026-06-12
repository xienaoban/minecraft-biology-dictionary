package io.github.xienaoban.biologydictionary.platform.gui.screen.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.TextureInfo;
import io.github.xienaoban.biologydictionary.platform.gui.screen.CommonScreen;
import io.github.xienaoban.biologydictionary.platform.gui.screen.ElementScreen;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

@ClientOnly
public final class ScreenRenderingContext {
	private static final int DEFAULT_TOOLTIP_WIDTH = 180;

	private final Screen screen;
	private final Minecraft client;

	private GuiGraphicsExtractor guiGraphics;
	private float screenScale;
	private float reciprocalScreenScale;
	private float mouseX;
	private float mouseY;
	private float tickDelta;
	private boolean debug;

	public ScreenRenderingContext(Screen screen) {
		client = Objects.requireNonNull(ClientUtils.getClient());
		this.screen = screen;
		screenScale = 1F;
		reciprocalScreenScale = 1F;
		debug = false;
	}

	public void update(GuiGraphicsExtractor guiGraphics, float screenScale, float reciprocalScreenScale, int mouseX, int mouseY, float tickDelta) {
		this.guiGraphics = guiGraphics;
		this.screenScale = screenScale;
		this.reciprocalScreenScale = reciprocalScreenScale;
		this.tickDelta = tickDelta;
		this.mouseX = (float) client.mouseHandler.xpos() * (float) client.getWindow().getGuiScaledWidth() / (float) client.getWindow().getScreenWidth();
		this.mouseY = (float) client.mouseHandler.ypos() * (float) client.getWindow().getGuiScaledHeight() / (float) client.getWindow().getScreenHeight();
		this.mouseX *= reciprocalScreenScale;
		this.mouseY *= reciprocalScreenScale;
	}

	public Minecraft getClient() { return client; }
	public Screen getScreen() { return screen; }
	public GuiGraphicsExtractor getGuiGraphics() { return guiGraphics; }
	public float getMouseX() { return mouseX; }
	public float getMouseY() { return mouseY; }
	public float getTickDelta() { return tickDelta; }
	public Font getFont() { return screen.getFont(); }
	public float getZ() { return getCommonScreen().getZ(); }
	public boolean isDebug() { return debug; }
	public void setDebug(boolean debug) { this.debug = debug; }
	public CommonScreen getCommonScreen() { return (CommonScreen) screen; }
	public ElementScreen getElementScreen() { return (ElementScreen) screen; }

	public Matrix3x2fStack getPose() {
		return getGuiGraphics().pose();
	}

	public void nextStratum() {
		getGuiGraphics().nextStratum();
	}

	public Object getScissorStack() {
		return null;
	}

	public Object getGuiRenderState() {
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

	public int calcTextWidth(FormattedCharSequence text) {
		return getFont().width(text);
	}

	public void renderText(FormattedCharSequence text, int color, float z, float x, float y) {
		getGuiGraphics().text(getFont(), text, (int) x, (int) y, color, false);
	}

	public void renderText(FormattedCharSequence text, int color, float size, float z, float x, float y) {
		try (ScaleRAII ignored = scaleOnce(size)) {
			renderText(text, color, z, x / size, y / size);
		}
	}

	public void renderText(Component component, int color, float z, float x, float y) {
		getGuiGraphics().text(getFont(), component, (int) x, (int) y, color, false);
	}

	public void renderText(Component component, int color, float size, float z, float x, float y) {
		try (ScaleRAII ignored = scaleOnce(size)) {
			renderText(component, color, z, x / size, y / size);
		}
	}

	public void renderCenteredText(Component component, int color, float z, float x, float y) {
		getGuiGraphics().centeredText(getFont(), component, (int) x, (int) y, color);
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
	}

	public void renderRectangle(int color, float z, float left, float top, float right, float bottom) {
		getGuiGraphics().fill((int) left, (int) top, (int) right, (int) bottom, color);
	}

	public void renderTexture(TextureInfo texture, float left, float top) {
		renderTexture(texture, left, top, texture.width(), texture.height());
	}

	public void renderTexture(TextureInfo texture, float left, float top, float width, float height) {
		renderTexture(texture, RenderPipelines.GUI_TEXTURED, left, top, width, height);
	}

	public void renderTexture(TextureInfo texture, RenderPipeline pipeline, float left, float top, float width, float height) {
		getGuiGraphics().blit(pipeline, texture.location(), (int) left, (int) top, 0, 0, (int) width, (int) height,
				(int) texture.width(), (int) texture.height());
	}

	public void renderTexture(TextureInfo texture, float left, float top, float u0, float v0, float u1, float v1) {
		getGuiGraphics().blit(RenderPipelines.GUI_TEXTURED, texture.location(), (int) left, (int) top, u0, v0,
				(int) (u1 - u0), (int) (v1 - v0), (int) texture.width(), (int) texture.height());
	}

	public void renderItem(ItemStack itemStack, float left, float top) {
		getGuiGraphics().item(itemStack, (int) left, (int) top);
	}

	public void renderItem(ItemStack itemStack, float size, float left, float top) {
		try (ScaleRAII ignored = scaleOnce(size / 16F)) {
			renderItem(itemStack, left * 16F / size, top * 16F / size);
		}
	}

	public void renderEffect(Holder<MobEffect> effect, float left, float top) {
		Identifier id = net.minecraft.client.gui.Gui.getMobEffectSprite(effect);
		getGuiGraphics().blitSprite(RenderPipelines.GUI_TEXTURED, id, (int) left, (int) top, 18, 18);
	}

	public void renderEffect(Holder<MobEffect> effect, float size, float left, float top) {
		try (ScaleRAII ignored = scaleOnce(size / 18F)) {
			renderEffect(effect, left * 18F / size, top * 18F / size);
		}
	}

	public void renderComponentTooltip(List<Component> texts, float leftX, float topY) {
		getGuiGraphics().setComponentTooltipForNextFrame(getFont(), texts, (int) leftX, (int) topY);
	}

	public void renderComponentTooltip(List<Component> texts, float size, float leftX, float topY) {
		try (ScaleRAII ignored = scaleOnce(size)) {
			renderComponentTooltip(texts, leftX / size, topY / size);
		}
	}

	public void renderComponentTooltipCentered(List<Component> texts, float midX, float topY) {
		renderComponentTooltip(texts, midX, topY);
	}

	public void renderComponentTooltipCentered(List<Component> texts, float size, float midX, float topY) {
		renderComponentTooltip(texts, size, midX, topY);
	}

	public void renderLinedTooltip(List<FormattedCharSequence> lines, float leftX, float topY) {
		getGuiGraphics().setTooltipForNextFrame(getFont(), lines, (int) leftX, (int) topY);
	}

	public void renderLinedTooltip(List<FormattedCharSequence> lines, float size, float leftX, float topY) {
		try (ScaleRAII ignored = scaleOnce(size)) {
			renderLinedTooltip(lines, leftX / size, topY / size);
		}
	}

	public void renderLinedTooltipCentered(List<FormattedCharSequence> lines, float midX, float topY) {
		renderLinedTooltip(lines, midX, topY);
	}

	public void renderLinedTooltipCentered(List<FormattedCharSequence> lines, float size, float midX, float topY) {
		renderLinedTooltip(lines, size, midX, topY);
	}

	public void renderEntityBottomed(Entity entity, EntityRenderingCache cache, float bottom, float midX, float size) {
		renderEntityCentered(entity, cache, midX, bottom - size / 2F, size);
	}

	public void renderEntityCentered(Entity entity, EntityRenderingCache cache, float midX, float midY, float size) {
		renderEntityCentered(entity, cache, midX, midY, size, 0, 0, 0);
	}

	public void renderEntityCentered(Entity entity, EntityRenderingCache cache, float midX, float midY, float size, float rotX, float rotY, float rotZ) {
		float half = size / 2F;
		renderEntity(entity, cache, midX - half, midY - half, midX + half, midY + half, rotX, rotY, rotZ);
	}

	private void renderEntity(Entity entity, EntityRenderingCache cache, float left, float top, float right, float bottom,
							  float rotX, float rotY, float rotZ) {
		EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
		EntityRenderingCache renderingCache = cache == null ? new EntityRenderingCache(dispatcher, entity) : cache;
		EntityRenderState renderState = renderingCache.getRenderState(entity);
		RenderUtils.renderBodyOnly(renderState);
		getGuiGraphics().entity(renderState, 1F, new Vector3f(0, 0, 0), new Quaternionf(), null,
				(int) left, (int) top, (int) right, (int) bottom);
	}

	public void renderPlayerFace(AbstractClientPlayer player, float left, float top) {
		renderPlayerFace(player, left, top, 8);
	}

	public void renderPlayerFace(AbstractClientPlayer player, float left, float top, float size) {
		getGuiGraphics().skin(null, player.getSkin().body().texturePath(), 8, 8, 8, 8,
				(int) left, (int) top, (int) size, (int) size);
	}

	public static final class EntityRenderingCache {
		private final EntityRenderDispatcher renderDispatcher;
		private Entity entity;
		private EntityRenderState renderState;

		public EntityRenderingCache(EntityRenderDispatcher renderDispatcher, Entity entity) {
			this.renderDispatcher = renderDispatcher;
			this.entity = entity;
			this.renderState = RenderUtils.createRenderState(renderDispatcher, entity);
		}

		public EntityRenderState getRenderState(Entity entity) {
			if (this.entity != entity) {
				this.entity = entity;
				renderState = RenderUtils.createRenderState(renderDispatcher, entity);
			}
			return renderState;
		}
	}
}
