package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class Widget extends ScreenElement {
    public static final int WIDGET_WIDTH = 10, WIDGET_WIDTH_MARGIN = 4;
    public static final int WIDGET_HEIGHT = 10, WIDGET_HEIGHT_MARGIN = 6;
    public static final int TOOLTIP_WIDTH = 240;

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

    /**
     * Append comma-separated items to the tooltip, wrapping lines manually so that
     * each individual item is never split across two lines.
     */
    protected static void appendWrappedItems(List<Component> tooltip, ScreenRenderingContext ctx, List<Component> items) {
        List<Component> currentLine = new ArrayList<>();
        int lineWidth = 0;
        Component separator = TextUtils.comma();
        int separatorWidth = ctx.calcTextWidth(separator);
        int spacing = ctx.calcTextWidth(TextUtils.concat(separator, separator)) - 2 * separatorWidth;
        separatorWidth += 2 * spacing;

        for (Component item : items) {
            int itemWidth = ctx.calcTextWidth(item);

            if (!currentLine.isEmpty() && lineWidth + separatorWidth + itemWidth > TOOLTIP_WIDTH - 2) {
                tooltip.add(TextUtils.concat(currentLine, separator));
                currentLine = new ArrayList<>();
                lineWidth = 0;
            }

            currentLine.add(item);
            lineWidth += (currentLine.size() > 1 ? separatorWidth : 0) + itemWidth;
        }

        if (!currentLine.isEmpty()) {
            tooltip.add(TextUtils.concat(currentLine, separator));
        }
    }
}
