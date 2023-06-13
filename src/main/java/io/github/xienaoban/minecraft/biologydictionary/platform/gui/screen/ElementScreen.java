package io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen;

import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.minecraft.network.chat.Component;

public abstract class ElementScreen extends CommonScreen {
    protected final RootScreenElement rootScreenElement;
    protected ScreenElement focusedElement;

    protected ElementScreen(Component component) {
        super(component);
        this.rootScreenElement = new RootScreenElement();
        this.focusedElement = null;
    }

    @Override
    protected void init() {
        super.init();
        rootScreenElement.resize(width, height);
    }

    @Override
    protected void beforeRender(ScreenRenderingContext ctx) {
        super.beforeRender(ctx);
        updateFocusedElement(ctx.getMouseX(), ctx.getMouseY());
    }

    protected void render(ScreenRenderingContext ctx) {
        super.render(ctx);
        rootScreenElement.render(ctx);
    }

    public final ScreenElement getFocusedElement() { return focusedElement; }

    private void updateFocusedElement(int x, int y) {
        if (focusedElement == null || !focusedElement.isFocused(x, y)) {
            focusedElement = rootScreenElement.focus(x, y);
        } else {
            focusedElement = focusedElement.focus(x, y);
        }
    }


    /**
     * Relocate the sub elements of root element.
     * @param width the new width of the screen, same with this.width
     * @param height the new height of the screen, same with this.height
     */
    protected abstract void resizeBox(int width, int height);

    private final class RootScreenElement extends ScreenElement {
        @Override
        protected void renderContent(ScreenRenderingContext ctx) {}

        @Override
        public void resizeBox(int width, int height) {
            box.set(0, 0, width, height);
            ElementScreen.this.resizeBox(width, height);
        }
    }
}
