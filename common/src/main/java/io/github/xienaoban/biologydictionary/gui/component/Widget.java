package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class Widget extends ScreenElement {
    public static final int WIDGET_WIDTH = 10, WIDGET_WIDTH_MARGIN = 4;
    public static final int WIDGET_HEIGHT = 10, WIDGET_HEIGHT_MARGIN = 6;

    public static int calcHeight(int rows) {
        return (WIDGET_HEIGHT + WIDGET_HEIGHT_MARGIN) * rows - WIDGET_HEIGHT_MARGIN;
    }

    public static int calcWidth(int columns) {
        return (WIDGET_WIDTH + WIDGET_WIDTH_MARGIN) * columns - WIDGET_WIDTH_MARGIN;
    }

    private final int rows, columns;

    public Widget(int rows, int columns) {
        if (rows <= 0 || rows > Page.ROWS) {
            throw new IllegalStateException("Unexpected value: " + rows);
        }
        if (columns <= 0 || columns > Page.COLUMNS) {
            throw new IllegalStateException("Unexpected value: " + columns);
        }
        this.rows = rows;
        this.columns = columns;
        getBox().setSize(calcWidth(columns), calcHeight(rows));
    }

    public final int getRows() { return rows; }
    public final int getColumns() { return columns; }
}
