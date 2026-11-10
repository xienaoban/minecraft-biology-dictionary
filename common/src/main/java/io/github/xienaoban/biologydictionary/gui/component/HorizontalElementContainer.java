package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;

import java.util.ArrayList;
import java.util.List;

/**
 * A row of elements laid out left to right and centered vertically, used to build the
 * tool bars of the book screens.
 */
@ClientOnly
public final class HorizontalElementContainer extends ScreenElement {
    private static final float ELEMENT_PADDING = 2;

    private final List<ScreenElement> elements = new ArrayList<>();

    public HorizontalElementContainer() {
        super(false);
    }

    public HorizontalElementContainer addElement(ScreenElement element) {
        elements.add(element);
        element.setParent(this);
        resizeToFitElements();
        return this;
    }

    private void resizeToFitElements() {
        float width = 0;
        float height = 0;
        for (ScreenElement element : elements) {
            if (width > 0) { width += ELEMENT_PADDING; }
            width += element.getBox().getWidth();
            height = Math.max(height, element.getBox().getHeight());
        }
        getBox().setSize(width, height);
    }

    @Override
    protected void onResize(int width, int height) {
        ScreenElementBox box = getBox();
        float elementLeft = box.getLeft();
        for (ScreenElement element : elements) {
            ScreenElementBox elementBox = element.getBox();
            elementBox.setPosition(elementLeft, box.getTop() + (box.getHeight() - elementBox.getHeight()) / 2);
            elementLeft += elementBox.getWidth() + ELEMENT_PADDING;
        }
    }
}
