package io.github.xienaoban.biologydictionary.platform.gui.screen;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.dialog.Dialog;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.minecraft.network.chat.Component;

@ClientOnly
public abstract class ElementScreen extends CommonScreen {
    private final RootScreenElement rootScreenElement;
    private ScreenElement hoveredElement;
    private ScreenElement selectedElement;

    private int ticks;

    protected ElementScreen(Component component) {
        super(component);
        rootScreenElement = new RootScreenElement();
        hoveredElement = null;
        ticks = 0;
    }

    @Override
    protected void resize() {
        super.resize();
        updateBoxSizes();
    }

    @Override
    public void tick() {
        try {
            super.tick();
            ++ticks; // yes the first `ticks` will be 1 instead of 0
            rootScreenElement.tick(ticks);
        } catch (Throwable e) {
            showExceptionMessageAndCloseScreen("Error in tick on screen", e);
        }
    }

    @Override
    protected boolean mouseClicked(float mouseX, float mouseY, int button) {
        try {
            updateSelectedElement();
            if (getSelectedElement() != null) {
                return getSelectedElement().mouseDown(mouseX, mouseY, button);
            } else {
                return super.mouseClicked(mouseX, mouseY, button);
            }
        } catch (Throwable e) {
            showExceptionMessageAndCloseScreen("Error in mouse clicking on screen", e);
            return true;
        }
    }

    @Override
    protected void beforeRender(ScreenRenderingContext ctx) {
        super.beforeRender(ctx);
        updateHoveredElement(ctx.getMouseX(), ctx.getMouseY());
    }

    @Override
    protected void render(ScreenRenderingContext ctx) {
        try {
            super.render(ctx);
            rootScreenElement.render(ctx);

            ctx.nextStratum();
            ScreenElement hovered = getHoveredElement();
            if (hovered != null) {
                hovered.renderHovered(ctx);
            }
        } catch (Throwable e) {
            showExceptionMessageAndCloseScreen("Error in rendering on screen", e);
        }
    }

    public final ScreenElement getHoveredElement() {
        return hoveredElement;
    }

    public final ScreenElement getSelectedElement() {
        return selectedElement;
    }

    public final ScreenElement getRootScreenElement() {
        return rootScreenElement;
    }

    public final void updateBoxSizes() {
        rootScreenElement.resize(screenRenderingContext.getScreenWidth(), screenRenderingContext.getScreenHeight());
    }

    /**
     * Show a modal {@link Dialog} on top of the current screen. The dialog is
     * mounted under {@link #getRootScreenElement()} at the highest priority so
     * it captures all hover/click input and is painted above every other
     * element. It is removed by {@link Dialog#close()} (clicking the backdrop,
     * the close button, or any added button).
     */
    public void showDialog(Dialog dialog) {
        dialog.setParent(rootScreenElement);
        updateBoxSizes();
    }

    public final int getTicks() {
        return ticks;
    }

    /**
     * Relocate the sub elements of root element.
     * @param width the scaled width of the screen after applying Biology Dictionary screen scale
     * @param height the scaled height of the screen after applying Biology Dictionary screen scale
     */
    protected abstract void resizeBox(int width, int height);

    private void updateHoveredElement(float x, float y) {
        // Full search each frame. The old incremental shortcut only descended from
        // the last hovered element, which assumes sibling elements never overlap —
        // but fixed elements (e.g. the discovery bar) overlap their page, so a
        // pointer moving from the page into the bar would stay stuck on the page.
        hoveredElement = rootScreenElement.hover(x, y);
    }

    private void updateSelectedElement() {
        ScreenElement element = getHoveredElement();
        while (element != null && !element.isSelectable()) {
            element = element.getParent();
        }
        selectedElement = element;
    }

    private void showExceptionMessageAndCloseScreen(String message, Throwable throwable) {
        onClose();
        BiologyDictionaryClient.printThrowableToLoggerAndGame(message, throwable);
    }

    private final class RootScreenElement extends ScreenElement {
        private RootScreenElement() {
            super(false);
        }

        @Override
        public void onResize(int width, int height) {
            getBox().set(0, 0, width, height);
            ElementScreen.this.resizeBox(width, height);
        }
    }
}
