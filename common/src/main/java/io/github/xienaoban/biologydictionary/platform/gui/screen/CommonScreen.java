package io.github.xienaoban.biologydictionary.platform.gui.screen;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.mixin.rendering.ScreenIMixin;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScaleRAII;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenConsts;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Why wrap the rendering APIs here?
 * 1. Because the APIs always change between different MC versions.
 * 2. Because the parameter names of the methods are missing when using the official mappings.
 */
@ClientOnly
public abstract class CommonScreen extends Screen implements ScreenConsts {
    private static boolean commonScreenOpened = false;

    // Avoid using Screen.width/height in subclasses. Biology Dictionary screens
    // apply a second scale, so layout code must use ScreenRenderingContext sizes.
    @SuppressWarnings("unused")
    private final int width = 0;
    @SuppressWarnings("unused")
    private final int height = 0;

    public static boolean isOpened() {
        return commonScreenOpened;
    }

    protected final ScreenRenderingContext screenRenderingContext;

    private Screen lastScreen;

    protected CommonScreen(Component component) {
        super(component);
        this.screenRenderingContext = new ScreenRenderingContext(this);
        this.lastScreen = null;
    }

    @Override
    protected final void init() {
        screenRenderingContext.update(super.width, super.height, getFont(), getZ(), ConfigsManager.getClient().getScreenScale());
        super.init();
        resize();
    }

    protected void resize() {}

    @Override
    public final void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
        ScreenRenderingContext ctx = screenRenderingContext;
        ctx.update(guiGraphics, mouseX, mouseY, tickDelta);
        try (ScaleRAII ignored = ctx.scaleOnce(ctx.getScreenScale())) {
            beforeRender(ctx);
            render(ctx);
            afterRender(ctx);
        }
    }

    @Override
    public final void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {}

    public final void renderBlurredBackground(ScreenRenderingContext ctx, float blurRadius) {
        try (ScaleRAII ignored = ctx.scaleToOriginalOnce()) {
            super.renderBlurredBackground(blurRadius);
        }
    }

    public final void renderTransparentBackground(ScreenRenderingContext ctx) {
        try (ScaleRAII ignored = ctx.scaleToOriginalOnce()) {
            super.renderTransparentBackground(ctx.getGuiGraphics());
        }
    }

    protected void beforeRender(ScreenRenderingContext ctx) {}

    protected void render(ScreenRenderingContext ctx) {}

    protected void afterRender(ScreenRenderingContext ctx) {
        try (ScaleRAII ignored = ctx.scaleToOriginalOnce()) {
            super.render(ctx.getGuiGraphics(), (int) ctx.getRawMouseX(), (int) ctx.getRawMouseY(), ctx.getTickDelta());
        }
    }

    @Override
    public final boolean mouseClicked(double mouseX, double mouseY, int button) {
        ScreenRenderingContext ctx = screenRenderingContext;
        if (mouseClicked(ctx.calcScaledValue((float) mouseX), ctx.calcScaledValue((float) mouseY), button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected boolean mouseClicked(float mouseX, float mouseY, int button) { return false; }

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
