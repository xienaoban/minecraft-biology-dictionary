package io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util;

import java.util.ArrayList;

/**
 * A rectangular element on the screen.
 * Each element does not overlap in pairs.
 */
public abstract class ScreenElement {
    protected ScreenElement parent;
    protected final ScreenElementBox box;
    protected final ArrayList<ScreenElement> subScreenElements;

    public ScreenElement() {
        this.parent = null;
        this.box = new ScreenElementBox();
        this.subScreenElements = new ArrayList<>();
    }

    public final ScreenElementBox getBox() { return box; }

    public final ScreenElement getParent() { return parent; }

    public final void setParent(ScreenElement newParent) {
        ScreenElement oldParent = parent;
        if (oldParent != null) {
            oldParent.unregisterSubScreenElement(this);
        }
        parent = newParent;
        if (newParent != null) {
            newParent.registerSubScreenElement(this);
        }
    }

    public final void resize(int width, int height) {
        resizeBox(width, height);
        for (ScreenElement subEle : subScreenElements) {
            subEle.resize(width, height);
        }
    }

    public final void render(ScreenRenderingContext ctx) {
        renderContent(ctx);
        if (ctx.isDebug()) {
            final int color;
            if (this == ctx.getScreen().getFocusedElement()) color = 0xFF00FF00;
            else if (isFocused(ctx.getMouseX(), ctx.getMouseY())) color = 0xFF0000FF;
            else color = 0xFFFF0000;
            ctx.getScreen().renderRectangle(ctx.getPoseStack(), color, 0.5F, 0,
                    box.getLeft(), box.getTop(), box.getRight(), box.getBottom());
        }
        for (ScreenElement subEle : subScreenElements) {
            subEle.render(ctx);
        }
    }

    public final ScreenElement focus(float x, float y) {
        if (!isFocused(x, y)) return null;
        for (ScreenElement sub : subScreenElements) {
            ScreenElement res = sub.focus(x, y);
            if (res != null) return res;
        }
        return this;
    }

    public final boolean isFocused(float x, float y) {
        return x >= box.getLeft() && x <= box.getRight() && y >= box.getTop() && y <= box.getBottom();
    }

    /**
     * Render the content of the current element.
     * @param ctx the context of the screen
     */
    protected abstract void renderContent(ScreenRenderingContext ctx);

    /**
     * Resize the width and height of the current element.
     * And also relocate the sub elements.
     * @param width the new width of the screen, same with this.width
     * @param height the new height of the screen, same with this.height
     */
    protected abstract void resizeBox(int width, int height);

    private void registerSubScreenElement(ScreenElement sub) {
        subScreenElements.add(sub);
    }

    private void unregisterSubScreenElement(ScreenElement sub) {
        subScreenElements.remove(sub);
    }

    public void clearSubScreenElements() {
        subScreenElements.clear();
    }
}
