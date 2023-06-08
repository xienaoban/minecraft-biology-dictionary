package io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.minecraft.network.chat.Component;

public abstract class ElementScreen extends CommonScreen {
    protected final RootScreenElement rootScreenElement;
    protected ScreenElement focusedElement;
    protected final ScreenRenderingContext screenRenderingContext;

    protected ElementScreen(Component component) {
        super(component);
        this.rootScreenElement = new RootScreenElement();
        this.focusedElement = null;
        this.screenRenderingContext = new ScreenRenderingContext(this);
    }

    @Override
    protected void init() {
        super.init();
        rootScreenElement.resize(width, height);
    }

    @Override
    public final void render(PoseStack poseStack, int mouseX, int mouseY, float tickDelta) {
        updateFocusedElement(mouseX, mouseY);
        screenRenderingContext.update(poseStack, mouseX, mouseY, tickDelta);
        render(screenRenderingContext);
    }

    protected void render(ScreenRenderingContext ctx) {
        rootScreenElement.render(ctx);
        super.render(ctx.getPoseStack(), ctx.getMouseX(), ctx.getMouseY(), ctx.getTickDelta());
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
