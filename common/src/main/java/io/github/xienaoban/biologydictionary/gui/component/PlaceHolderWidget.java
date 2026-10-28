package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;

/**
 * An empty grid cell of a page, used to skip slots when laying out widgets.
 */
@ClientOnly
public final class PlaceHolderWidget extends Widget {
    public PlaceHolderWidget(int rows, int columns) {
        super(rows, columns);
        setHoverable(false);
        setSelectable(false);
    }
}
