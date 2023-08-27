package io.github.xienaoban.minecraft.biologydictionary.gui.component;

import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class Widget extends ScreenElement {
    public static final int WIDGET_WIDTH = 12, WIDGET_WIDTH_MARGIN = 8;
    public static final int WIDGET_HEIGHT = 12, WIDGET_HEIGHT_MARGIN = 6;

    private final int rows, columns;

    protected Widget(int rows, int columns) {
        if (rows <= 0 || rows > Page.ROWS) {
            throw new IllegalStateException("Unexpected value: " + rows);
        }
        if (columns <= 0 || columns > Page.COLUMNS) {
            throw new IllegalStateException("Unexpected value: " + columns);
        }
        int height = (WIDGET_HEIGHT + WIDGET_HEIGHT_MARGIN) * rows - WIDGET_HEIGHT_MARGIN;
        int width = (WIDGET_WIDTH + WIDGET_WIDTH_MARGIN) * columns - WIDGET_WIDTH_MARGIN;
        this.rows = rows;
        this.columns = columns;
        getBox().setSize(width, height);
    }

    public final int getRows() { return rows; }
    public final int getColumns() { return columns; }
}
