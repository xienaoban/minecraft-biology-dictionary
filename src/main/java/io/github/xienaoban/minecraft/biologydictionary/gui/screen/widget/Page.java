package io.github.xienaoban.minecraft.biologydictionary.gui.screen.widget;

import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;

public final class Page extends ScreenElement {
    public static final int PAGE_WIDTH = 110, PAGE_HEIGHT = 150;

    public Page() {
        box.setSize(PAGE_WIDTH, PAGE_HEIGHT);
    }

    @Override
    protected void renderContent(ScreenRenderingContext ctx) {}

    @Override
    protected void resizeBox(int width, int height) {

    }
}
