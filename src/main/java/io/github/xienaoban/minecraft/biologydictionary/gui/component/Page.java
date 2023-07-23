package io.github.xienaoban.minecraft.biologydictionary.gui.component;

import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class Page extends ScreenElement {
    public static final int PAGE_WIDTH = 112, PAGE_HEIGHT = 142;
    public static final int ROWS = 8, COLUMNS = 6;

    private final Widget[][] widgetLayout;  // widgetLayout[row][column]

    public Page() {
        super(false);
        widgetLayout = new Widget[ROWS][COLUMNS];
        getBox().setSize(PAGE_WIDTH, PAGE_HEIGHT);
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        // render nothing for now
    }

    @Override
    protected void onResize(int width, int height) {
        for (int r = 0; r < ROWS; ++r) for (int c = 0; c < COLUMNS; ++c) {
            Widget widget = widgetLayout[r][c];
            if (widget == null || widget instanceof WidgetPlaceholder) continue;
            float left = getBox().getLeft() + (Widget.WIDGET_WIDTH + Widget.WIDGET_WIDTH_MARGIN) * c;
            float top = getBox().getTop() + (Widget.WIDGET_HEIGHT + Widget.WIDGET_HEIGHT_MARGIN) * r;
            widget.getBox().setPosition(left, top);
        }
    }

    public boolean hasWidget(int row, int col) {
        return widgetLayout[row][col] != null;
    }

    public Widget getWidget(int row, int col) {
        if (!hasWidget(row, col)) return null;
        Widget widget = widgetLayout[row][col];
        if (widget instanceof WidgetPlaceholder placeholder) {
            widget = widgetLayout[placeholder.getHolderRow()][placeholder.getHolderCol()];
        }
        return widget;
    }

    public boolean addWidget(Widget widget) {
        for (int r = 0; r < ROWS; ++r) for (int c = 0; c < COLUMNS; ++c) {
            if (hasWidget(r, c)) continue;
            if (setWidget(widget, r, c)) return true;
        }
        return false;
    }

    public boolean setWidget(Widget widget, int row, int col) {
        int rowEnd = row + widget.getRows();
        int colEnd = col + widget.getColumns();
        if (rowEnd > ROWS || colEnd > COLUMNS) return false;
        for (int r = row; r < rowEnd; ++r) for (int c = col; c < colEnd; ++c) {
            if (hasWidget(r, c)) return false;
        }
        if (widget.getRows() * widget.getColumns() > 1) {
            WidgetPlaceholder placeholder = new WidgetPlaceholder(row, col);
            for (int r = row; r < rowEnd; ++r) for (int c = col; c < colEnd; ++c) {
                widgetLayout[r][c] = placeholder;
            }
        }
        widgetLayout[row][col] = widget;
        widget.setParent(this);
        return true;
    }

    private static final class WidgetPlaceholder extends Widget {
        private final int holderRow, holderCol;

        public WidgetPlaceholder(int holderRow, int holderCol) {
            super(1, 1);
            this.holderRow = holderRow;
            this.holderCol = holderCol;
            setSelectable(false);
        }

        public int getHolderRow() { return holderRow; }
        public int getHolderCol() { return holderCol; }
    }
}
