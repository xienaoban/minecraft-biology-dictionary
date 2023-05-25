package io.github.xienaoban.minecraft.biologydictionary.gui.screen.util;

import java.util.ArrayList;

/**
 * A rectangular element on the screen.
 * Each element does not overlap in pairs.
 */
public class ScreenElement {
    protected final ScreenElement parent;
    protected final ScreenElementBox box;
    protected final ArrayList<ScreenElement> subScreenElements;

    public ScreenElement(ScreenElement parent) {
        this.parent = parent;
        this.box = new ScreenElementBox();
        this.subScreenElements = new ArrayList<>();

        if (parent != null) {
            parent.registerSubScreenElement(this);
        }
    }

    public ScreenElement getParent() { return parent; }
    public ScreenElementBox getBox() { return box; }

    public void render(ScreenRenderingContext ctx) {
        if (ctx.shouldRenderBox()) {
            ctx.getScreen().renderRectangle(ctx.getPoseStack(), 0xFFFF0000, 0.5F, 0,
                    box.getLeft(), box.getTop(), box.getRight(), box.getBottom());
        }
        for (ScreenElement subEle : subScreenElements) {
            subEle.render(ctx);
        }
    }

    private void registerSubScreenElement(ScreenElement sub) {
        subScreenElements.add(sub);
    }

    public void clearSubScreenElements() {
        subScreenElements.clear();
    }
}
