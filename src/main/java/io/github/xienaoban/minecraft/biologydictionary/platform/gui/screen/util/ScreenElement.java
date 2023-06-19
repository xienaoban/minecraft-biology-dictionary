package io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util;

import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.ElementScreen;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * A rectangular element on the screen.
 * Each element does not overlap in pairs.
 */
public abstract class ScreenElement {
    @Nullable protected ScreenElement parent;
    protected final ScreenElementBox box;
    protected final ArrayList<ScreenElement> subScreenElements;

    public ScreenElement() {
        this.parent = null;
        this.box = new ScreenElementBox();
        this.subScreenElements = new ArrayList<>();
    }

    public final ScreenElementBox getBox() { return box; }

    @Nullable public final ScreenElement getParent() { return parent; }

    public final void setParent(ScreenElement newParent) {
        setParent(newParent, false);
    }

    public final void setParent(ScreenElement newParent, boolean highPriority) {
        ScreenElement oldParent = parent;
        if (oldParent != null) {
            oldParent.unregisterSubScreenElement(this);
        }
        parent = newParent;
        if (newParent != null) {
            newParent.registerSubScreenElement(this, highPriority);
        }
    }

    public final void resize(int width, int height) {
        onResize(width, height);
        for (ScreenElement subEle : subScreenElements) {
            subEle.resize(width, height);
        }
    }

    public final void render(ScreenRenderingContext ctx) {
        onRender(ctx);
        if (ctx.isDebug() && box.getWidth() * box.getHeight() > 0) {
            final int color;
            if (this == ((ElementScreen) ctx.getScreen()).getSelectedElement()) color = 0xFF00FF00;
            else if (this == ((ElementScreen) ctx.getScreen()).getFocusedElement()) color = 0xFFFFFF00;
            else if (isFocused(ctx.getMouseX(), ctx.getMouseY())) color = 0xFF0000FF;
            else color = 0xFFFF0000;
            ctx.getScreen().renderRectangle(ctx, color, 1, ctx.getScreen().getZ(),
                    (int) box.getLeft(), (int) box.getTop(), (int) box.getRight(), (int) box.getBottom());
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
        return x > box.getLeft() && x < box.getRight() && y > box.getTop() && y < box.getBottom();
    }

    public final boolean mouseDown(float x, float y, int code) {
        return !onMouseDown(x, y, code) && getParent() != null && getParent().mouseDown(x, y , code);
    }

    /**
     * Render the content of the current element.
     * @param ctx the context of the screen
     */
    protected void onRender(ScreenRenderingContext ctx) {}

    /**
     * Resize the width and height of the current element.
     * And also relocate the sub elements.
     * @param width the new width of the screen, same with this.width
     * @param height the new height of the screen, same with this.height
     */
    protected void onResize(int width, int height) {}

    /**
     * MouseDown event.
     * @param x the x of mouse
     * @param y the y of mouse
     * @param code the mouse code
     * @return whether to consume the event (return false to pass the event to the parent element)
     */
    protected boolean onMouseDown(float x, float y, int code) { return false; }

    private void registerSubScreenElement(ScreenElement sub, boolean highPriority) {
        if (highPriority) {
            subScreenElements.add(0, sub);
        } else {
            subScreenElements.add(sub);
        }
    }

    private void unregisterSubScreenElement(ScreenElement sub) {
        subScreenElements.remove(sub);
    }

    public void clearSubScreenElements() {
        subScreenElements.clear();
    }
}
