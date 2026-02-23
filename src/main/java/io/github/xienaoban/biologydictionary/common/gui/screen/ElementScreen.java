package io.github.xienaoban.biologydictionary.common.gui.screen;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
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
            showExceptionMessageAndCloseScreen(e);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        try {
            updateSelectedElement();
            if (getSelectedElement() != null) {
                return getSelectedElement().mouseDown((float) mouseButtonEvent.x(), (float) mouseButtonEvent.y(), mouseButtonEvent.button());
            } else {
                return super.mouseClicked(mouseButtonEvent, bl);
            }
        } catch (Throwable e) {
            showExceptionMessageAndCloseScreen(e);
        }
        return true;
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

            ctx.getGuiGraphics().nextStratum();
            ScreenElement hovered = getHoveredElement();
            if (hovered != null) {
                hovered.renderHovered(ctx);
            }
        } catch (Throwable e) {
            showExceptionMessageAndCloseScreen(e);
        }
    }

    public final ScreenElement getHoveredElement() {
        return hoveredElement;
    }

    private void updateHoveredElement(float x, float y) {
        if (hoveredElement != null && hoveredElement.isHovered(x, y)) {
            hoveredElement = hoveredElement.hover(x, y);
        } else {
            hoveredElement = rootScreenElement.hover(x, y);
        }
    }

    public final void clearHoveredElement() {
        hoveredElement = null;
    }

    public final ScreenElement getSelectedElement() {
        return selectedElement;
    }

    private void updateSelectedElement() {
        ScreenElement element = getHoveredElement();
        while (element != null && !element.isSelectable()) {
            element = element.getParent();
        }
        selectedElement = element;
    }

    public final ScreenElement getRootScreenElement() {
        return rootScreenElement;
    }

    public final void updateBoxSizes() {
        rootScreenElement.resize(width, height);
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
        onClose();
        BiologyDictionaryClient.printThrowableToLoggerAndGame(throwable);
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
