package io.github.xienaoban.minecraft.biologydictionary.gui.screen.widget;

import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.util.TranslationKeys;
import net.minecraft.network.chat.Component;

public final class Page extends ScreenElement {
    public static final int PAGE_WIDTH = 110, PAGE_HEIGHT = 142;
    public static final int ROWS = 8, COLUMNS = 2;

    private final Widget[][] widgetLayout;

    public Page() {
        widgetLayout = new Widget[ROWS][COLUMNS];
        box.setSize(PAGE_WIDTH, PAGE_HEIGHT);
        for (int i = 0; i < 5; ++i) {
            addWidget(new Widget(1, 1) {
                @Override
                protected void renderContent(ScreenRenderingContext ctx) {
                    ctx.getScreen().renderCenteredText(ctx, Component.translatable(TranslationKeys.BIOLOGY_DICTIONARY_TITLE), 0xFF0000FF, (int) box.getLeft() + Widget.WIDGET_WIDTH / 2, (int) box.getTop() + 4);
                }

                @Override
                protected void resizeBox(int width, int height) {}
            });
        }
        addWidget(new Widget(3, 2) {
            @Override
            protected void renderContent(ScreenRenderingContext ctx) {}

            @Override
            protected void resizeBox(int width, int height) {}
        });
        for (int i = 0; i < 8; ++i) {
            addWidget(new Widget(1, 1) {
                @Override
                protected void renderContent(ScreenRenderingContext ctx) {
                    ctx.getScreen().renderCenteredText(ctx, Component.translatable(TranslationKeys.BIOLOGY_DICTIONARY_TITLE), 0xFF0000FF, (int) box.getLeft() + Widget.WIDGET_WIDTH / 2, (int) box.getTop() + 4);
                }

                @Override
                protected void resizeBox(int width, int height) {}
            });
        }
    }

    @Override
    protected void renderContent(ScreenRenderingContext ctx) {}

    @Override
    protected void resizeBox(int width, int height) {
        for (int r = 0; r < ROWS; ++r) for (int c = 0; c < COLUMNS; ++c) {
            Widget widget = widgetLayout[r][c];
            if (widget == null || widget instanceof WidgetPlaceholder) continue;
            float left = box.getLeft() + (Widget.WIDGET_WIDTH + Widget.WIDGET_WIDTH_MARGIN) * c;
            float top = box.getTop() + (Widget.WIDGET_HEIGHT + Widget.WIDGET_HEIGHT_MARGIN) * r;
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
            this.holderRow = holderRow;
            this.holderCol = holderCol;
        }

        public int getHolderRow() { return holderRow; }
        public int getHolderCol() { return holderCol; }

        @Override
        protected void renderContent(ScreenRenderingContext ctx) {}

        @Override
        protected void resizeBox(int width, int height) {}
    }
}
