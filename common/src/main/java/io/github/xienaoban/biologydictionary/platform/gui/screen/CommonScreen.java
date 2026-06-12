package io.github.xienaoban.biologydictionary.platform.gui.screen;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScaleRAII;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenConsts;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

@ClientOnly
public abstract class CommonScreen extends Screen implements ScreenConsts {
	private static boolean commonScreenOpened = false;

	protected final ScreenRenderingContext screenRenderingContext;

	private Screen lastScreen;
	private float screenScale;
	private float reciprocalScreenScale;

	protected CommonScreen(Component component) {
		super(component);
		screenRenderingContext = new ScreenRenderingContext(this);
		lastScreen = null;
	}

	public static boolean isOpened() {
		return commonScreenOpened;
	}

	@Override
	protected final void init() {
		screenScale = ConfigsManager.getClient().getScreenScale();
		reciprocalScreenScale = 1.0F / screenScale;
		width = Mth.ceil(width * reciprocalScreenScale);
		height = Mth.ceil(height * reciprocalScreenScale);
		super.init();
		resize();
	}

	protected void resize() {}

	@Override
	public final void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta) {
		screenRenderingContext.update(guiGraphics, screenScale, reciprocalScreenScale, mouseX, mouseY, tickDelta);
		try (ScaleRAII ignored = screenRenderingContext.scaleOnce(screenScale)) {
			beforeRender(screenRenderingContext);
			render(screenRenderingContext);
			afterRender(screenRenderingContext);
		}
	}

	@Override
	public final void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta) {}

	public final void renderBlurredBackground(ScreenRenderingContext ctx) {
		super.extractBlurredBackground(ctx.getGuiGraphics());
	}

	public final void renderTransparentBackground(ScreenRenderingContext ctx) {
		super.extractTransparentBackground(ctx.getGuiGraphics());
	}

	protected void beforeRender(ScreenRenderingContext ctx) {}

	protected void render(ScreenRenderingContext ctx) {}

	protected void afterRender(ScreenRenderingContext ctx) {
		super.extractRenderState(ctx.getGuiGraphics(), (int) ctx.getMouseX(), (int) ctx.getMouseY(), ctx.getTickDelta());
	}

	public Font getFont() {
		return super.getFont();
	}

	public float getZ() {
		return 0;
	}

	@Override
	public void added() {
		super.added();
		commonScreenOpened = true;
		LOGGER.info("Screen {} opened.", getClass().getSimpleName());
	}

	@Override
	public void removed() {
		super.removed();
		commonScreenOpened = false;
		LOGGER.info("Screen {} closed.", getClass().getSimpleName());
	}

	@Override
	public void onClose() {
		if (lastScreen != null) {
			ClientUtils.setScreen(lastScreen);
		} else {
			super.onClose();
		}
	}

	public Screen getLastScreen() {
		return lastScreen;
	}

	public void setLastScreen(Screen lastScreen) {
		this.lastScreen = lastScreen;
	}
}
