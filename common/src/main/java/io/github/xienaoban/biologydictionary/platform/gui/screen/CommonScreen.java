package io.github.xienaoban.biologydictionary.platform.gui.screen;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScaleRAII;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenConsts;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Why wrap the rendering APIs here?
 * 1. Because the APIs always change between different MC versions.
 * 2. Because the parameter names of the methods are missing when using the official mappings.
 */
@ClientOnly
public abstract class CommonScreen extends Screen implements ScreenConsts {
    private static final int MAX_BACK_CHAIN_LENGTH = 16;

    private static boolean commonScreenOpened = false;

    // Avoid using Screen.width/height in subclasses. Biology Dictionary screens
    // apply a second scale, so layout code must use ScreenRenderingContext sizes.
    @SuppressWarnings("unused")
    private final int width = 0;
    @SuppressWarnings("unused")
    private final int height = 0;

    protected final ScreenRenderingContext screenRenderingContext;

    private Screen lastScreen;

    protected CommonScreen(Component component) {
        super(component);
        this.screenRenderingContext = new ScreenRenderingContext(this);
        this.lastScreen = null;
    }

    public static boolean isOpened() {
        return commonScreenOpened;
    }

    @Override
    protected final void init() {
        screenRenderingContext.update(super.width, super.height, getFont(), getZ(), ConfigsManager.getClient().getScreenScale());
        super.init();
        resize();
    }

    protected void resize() {}

    @Override
    public final void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta) {
        ScreenRenderingContext ctx = screenRenderingContext;
        ctx.update(guiGraphics, mouseX, mouseY, tickDelta);
        try (ScaleRAII ignored = ctx.scaleOnce(ctx.getScreenScale())) {
            beforeRender(ctx);
            render(ctx);
            afterRender(ctx);
        }
    }

    @Override
    public final void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta) {}

    public final void renderBlurredBackground(ScreenRenderingContext ctx) {
        try (ScaleRAII ignored = ctx.scaleToOriginalOnce()) {
            super.extractBlurredBackground(ctx.getGuiGraphics());
        }
    }

    public final void renderTransparentBackground(ScreenRenderingContext ctx) {
        try (ScaleRAII ignored = ctx.scaleToOriginalOnce()) {
            super.extractTransparentBackground(ctx.getGuiGraphics());
        }
    }

    protected void beforeRender(ScreenRenderingContext ctx) {}

    protected void render(ScreenRenderingContext ctx) {}

    protected void afterRender(ScreenRenderingContext ctx) {
        try (ScaleRAII ignored = ctx.scaleToOriginalOnce()) {
            super.extractRenderState(
                    ctx.getGuiGraphics(), (int) ctx.getRawMouseX(), (int) ctx.getRawMouseY(), ctx.getTickDelta());
        }
    }

    @Override
    public final boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        ScreenRenderingContext ctx = screenRenderingContext;
        if (mouseClicked(
                ctx.calcScaledValue((float) mouseButtonEvent.x()),
                ctx.calcScaledValue((float) mouseButtonEvent.y()),
                mouseButtonEvent.button())) {
            // A custom element handled the click, so it owns the interaction now: the
            // vanilla focus (a focused text box, for instance) must not survive it.
            clearFocus();
            return true;
        }
        if (super.mouseClicked(mouseButtonEvent, doubleClick)) {
            return true;
        }
        // The click hit nothing interactive (the book background, for instance), which
        // vanilla does not treat as a focus change by itself.
        clearFocus();
        return false;
    }

    protected boolean mouseClicked(float mouseX, float mouseY, int button) { return false; }

    /**
     * Whether a vanilla text input owns the keyboard. Implemented here rather than
     * relying on the vanilla method, so that every supported MC version behaves the same.
     */
    public boolean isInputCaptured() {
        GuiEventListener element = getFocusedElement();
        if (element instanceof EditBox editBox) {
            return editBox.canConsumeInput();
        }
        return element instanceof MultiLineEditBox editBox && editBox.visible && editBox.isFocused();
    }

    /**
     * The focus path nests the focused component inside its containers, so walk down to
     * the component that actually owns the input.
     */
    private GuiEventListener getFocusedElement() {
        ComponentPath focusPath = getCurrentFocusPath();
        if (focusPath == null) {
            return null;
        }
        while (focusPath instanceof ComponentPath.Path path) {
            focusPath = path.childPath();
        }
        return focusPath.component();
    }

    public Font getFont() { return super.getFont(); }

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

    /**
     * The fixed depth of this screen in the navigation chain:
     * <ul>
     *     <li>0 — the home screen, the root of every chain;</li>
     *     <li>1 — settings and search;</li>
     *     <li>2 — entity detail and overview;</li>
     *     <li>3 — modal screens opened on top of a detail page.</li>
     * </ul>
     * A negative value means the screen takes no part in the chain and simply returns
     * to {@link #getLastScreen()}, which is the behaviour of every screen by default.
     *
     * <p>Screens may be opened in any order, so the {@code lastScreen} chain is not
     * necessarily ordered by depth. {@link #onClose()} walks it and skips every screen
     * whose depth is not strictly smaller than this one, which keeps the chain the user
     * actually walks — home, then settings or search, then a detail page — while still
     * returning to the original instances, so their state (page index, query) survives.</p>
     */
    public int navigationLevel() {
        return -1;
    }

    @Override
    public void onClose() {
        Screen target = resolveBackScreen();
        if (target != null) {
            ClientUtils.setScreen(target);
        } else {
            super.onClose();
        }
    }

    /**
     * The screen {@link #onClose()} should show: the first screen up the chain whose
     * depth is strictly smaller than this one. Screens with a negative depth are skipped
     * as well, since they do not belong to the chain.
     */
    private Screen resolveBackScreen() {
        final int level = navigationLevel();
        Screen parent = lastScreen;
        if (level < 0) {
            return parent;
        }
        for (int guard = 0; parent != null && guard < MAX_BACK_CHAIN_LENGTH; ++guard) {
            if (!(parent instanceof CommonScreen screen)) {
                return parent;
            }
            int parentLevel = screen.navigationLevel();
            if (parentLevel >= 0 && parentLevel < level) {
                return parent;
            }
            parent = screen.getLastScreen();
        }
        return null;
    }

    public Screen getLastScreen() {
        return lastScreen;
    }

    public void setLastScreen(Screen lastScreen) {
        this.lastScreen = lastScreen;
    }
}
