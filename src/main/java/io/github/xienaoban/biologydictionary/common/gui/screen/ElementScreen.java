package io.github.xienaoban.biologydictionary.common.gui.screen;

import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public abstract class ElementScreen extends CommonScreen {
    private final RootScreenElement rootScreenElement;
    private ScreenElement focusedElement;
    private ScreenElement selectedElement;

    private int ticks;

    protected ElementScreen(Component component) {
        super(component);
        this.rootScreenElement = new RootScreenElement();
        this.focusedElement = null;

        this.ticks = 0;
    }

    @Override
    protected void init() {
        super.init();
        rootScreenElement.resize(width, height);
    }

    @Override
    public void tick() {
        try {
            super.tick();
            ++ticks; // yes the first `ticks` will be 1 instead of 0
            rootScreenElement.tick(ticks);
        } catch (RuntimeException e) {
            showExceptionMessageAndCloseScreen(e);
        }
    }

    @Override
    public final boolean mouseClicked(double x, double y, int code) {
        updateSelectedElement();
        if (getSelectedElement() != null) {
            return getSelectedElement().mouseDown((float) x, (float) y, code);
        } else {
            return super.mouseClicked(x, y, code);
        }
    }

    @Override
    protected void beforeRender(ScreenRenderingContext ctx) {
        super.beforeRender(ctx);
        updateFocusedElement(ctx.getMouseX(), ctx.getMouseY());
    }

    @Override
    protected void render(ScreenRenderingContext ctx) {
        try {
            super.render(ctx);
            rootScreenElement.render(ctx);
        } catch (RuntimeException e) {
            showExceptionMessageAndCloseScreen(e);
        }
    }

    public final ScreenElement getFocusedElement() {
        return focusedElement;
    }

    private void updateFocusedElement(float x, float y) {
        if (focusedElement != null && focusedElement.isFocused(x, y)) {
            focusedElement = focusedElement.focus(x, y);
        } else {
            focusedElement = rootScreenElement.focus(x, y);
        }
    }

    public final ScreenElement getSelectedElement() {
        return selectedElement;
    }

    private void updateSelectedElement() {
        ScreenElement element = getFocusedElement();
        while (element != null && !element.isSelectable()) {
            element = element.getParent();
        }
        selectedElement = element;
    }

    public final ScreenElement getRootScreenElement() {
        return rootScreenElement;
    }

    public final int getTicks() {
        return ticks;
    }

    /**
     * Relocate the sub elements of root element.
     * @param width the new width of the screen, same with this.width
     * @param height the new height of the screen, same with this.height
     */
    protected abstract void resizeBox(int width, int height);

    private void showExceptionMessageAndCloseScreen(Throwable throwable) {
        Misc.printThrowableToLoggerAndGame(throwable);
        onClose();
    }

    private final class RootScreenElement extends ScreenElement {
        public RootScreenElement() {
            super(false);
        }

        @Override
        public void onResize(int width, int height) {
            getBox().set(0, 0, width, height);
            ElementScreen.this.resizeBox(width, height);
        }
    }
}
