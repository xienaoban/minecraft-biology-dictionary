package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;

@ClientOnly
public final class Page extends ScreenElement {
    public static final int ROWS = 9, COLUMNS = 8;
    public static final int PAGE_WIDTH = Widget.calcWidth(COLUMNS), PAGE_HEIGHT = Widget.calcHeight(ROWS);

    private final Widget[][] widgetLayout;  // widgetLayout[row][column]

    // Row where addWidget starts scanning: normally the first row (from row 0) that
    // is not completely filled, but it can also be pushed forward manually for soft
    // page separation (memory barrier). Only affects addWidget, not setWidget.
    private int scanStartRow = 0;

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
        super.onResize(width, height);
        for (int r = 0; r < ROWS; ++r) for (int c = 0; c < COLUMNS; ++c) {
            Widget widget = widgetLayout[r][c];
            if (widget == null || widget instanceof WidgetPlaceholder) { continue; }
            float left = getBox().getLeft() + (Widget.WIDGET_WIDTH + Widget.WIDGET_WIDTH_MARGIN) * c;
            float top = getBox().getTop() + (Widget.WIDGET_HEIGHT + Widget.WIDGET_HEIGHT_MARGIN) * r;
            widget.getBox().setPosition(left, top);
        }
    }

    public boolean hasWidget(int row, int col) {
        return widgetLayout[row][col] != null;
    }

    public Widget getWidget(int row, int col) {
        if (!hasWidget(row, col)) { return null; }
        Widget widget = widgetLayout[row][col];
        if (widget instanceof WidgetPlaceholder placeholder) {
            widget = widgetLayout[placeholder.getHolderRow()][placeholder.getHolderCol()];
        }
        return widget;
    }

    public boolean addWidget(Widget widget) {
        for (int r = scanStartRow; r < ROWS; ++r) for (int c = 0; c < COLUMNS; c += widget.getColumns()) {
            if (hasWidget(r, c)) { continue; }
            if (setWidget(widget, r, c)) {
                if (r == scanStartRow) {
                    advanceScanStartRow();
                }
                return true;
            }
        }
        return false;
    }

    public boolean setWidget(Widget widget, int row, int col) {
        int rowEnd = row + widget.getRows();
        int colEnd = col + widget.getColumns();
        if (rowEnd > ROWS || colEnd > COLUMNS) { return false; }
        for (int r = row; r < rowEnd; ++r) for (int c = col; c < colEnd; ++c) {
            if (hasWidget(r, c)) { return false; }
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

    /**
     * Index of the last occupied row (0-based), or -1 if the page is empty.
     */
    public int getLastOccupiedRow() {
        for (int r = ROWS - 1; r >= 0; --r) {
            for (int c = 0; c < COLUMNS; ++c) {
                if (hasWidget(r, c)) { return r; }
            }
        }
        return -1;
    }

    /**
     * Push the scan start row forward so that widgets added afterward start on a new
     * row (skipping gapRows empty rows), leaving any gaps in earlier rows unfilled.
     */
    public void advanceScanStartRow(int gapRows) {
        scanStartRow = Math.min(ROWS, getLastOccupiedRow() + 1 + Math.max(0, gapRows));
    }

    private void advanceScanStartRow() {
        while (scanStartRow < ROWS && isRowFull(scanStartRow)) {
            scanStartRow++;
        }
    }

    private boolean isRowFull(int row) {
        for (int c = 0; c < COLUMNS; ++c) {
            if (!hasWidget(row, c)) { return false; }
        }
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
