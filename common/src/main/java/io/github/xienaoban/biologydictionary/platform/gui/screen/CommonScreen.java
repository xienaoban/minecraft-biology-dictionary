package io.github.xienaoban.biologydictionary.platform.gui.screen;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.mixin.rendering.ScreenIMixin;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScaleRAII;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenConsts;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Why wrap the rendering APIs here?
 * 1. Because the APIs always change between different MC versions.
 * 2. Because the parameter names of the methods are missing when using the official mappings.
 */
@Environment(EnvType.CLIENT)
public abstract class CommonScreen extends Screen implements ScreenConsts {
    private static boolean commonScreenOpened = false;

    public static boolean isOpened() {
        return commonScreenOpened;
    }

    protected final ScreenRenderingContext screenRenderingContext;

    private Screen lastScreen;

    /**
     * Screen scale factor for rendering the screen.
     * <p>
     * Relationship between GUI Scale and Screen Scale:
     * Actual Screen Size = Default Screen Size * GUI Scale * Screen Scale
     * <p>
     * For example: scale=2.0 makes UI elements appear twice as large,
     * while scale=0.5 makes them appear half as large.
     * </p>
     */
    private float screenScale, reciprocalScreenScale;

    protected CommonScreen(Component component) {
        super(component);
        this.screenRenderingContext = new ScreenRenderingContext(this);
        this.lastScreen = null;
    }

    @Override
    protected final void init() {
        screenScale = ConfigsManager.getClient().getScreenScale();
        reciprocalScreenScale = 1F / screenScale;
        super.width = Mth.ceil(super.width * reciprocalScreenScale);
        super.height = Mth.ceil(super.height * reciprocalScreenScale);
        super.init();
        resize();
    }

    protected void resize() {}

    @Override
    public final void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
        screenRenderingContext.update(guiGraphics, screenScale, reciprocalScreenScale, mouseX, mouseY, tickDelta);
        try (ScaleRAII ignored = screenRenderingContext.scaleOnce(screenScale)) {
            beforeRender(screenRenderingContext);
            render(screenRenderingContext);
            afterRender(screenRenderingContext);
        }
    }

    @Override
    public final void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {}

    public final void renderBlurredBackground(ScreenRenderingContext ctx, float blurRadius) {
        super.renderBlurredBackground(blurRadius);
    }

    public final void renderTransparentBackground(ScreenRenderingContext ctx) {
        super.renderTransparentBackground(ctx.getGuiGraphics());
    }

    protected void beforeRender(ScreenRenderingContext ctx) {}

    protected void render(ScreenRenderingContext ctx) {}

    protected void afterRender(ScreenRenderingContext ctx) {
        super.render(ctx.getGuiGraphics(), (int) ctx.getMouseX(), (int) ctx.getMouseY(), ctx.getTickDelta());
    }

    public Font getFont() { return ((ScreenIMixin) this).biologydictionary$getFont(); }

    public float getZ() { return 0; }

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
