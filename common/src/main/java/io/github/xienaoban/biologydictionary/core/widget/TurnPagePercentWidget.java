package io.github.xienaoban.biologydictionary.core.widget;

import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;

/**
 * Page turn marker: turns the page when the current page's occupied rows reach the
 * given percent (0~1); otherwise softly separates (widgets after it start on a new
 * row, with gapRows empty rows in between, and never fill gaps in earlier rows).
 * percent = 0 means always turn the page (unless the current page is empty).
 */
@ClientOnly
public final class TurnPagePercentWidget extends Widget implements TurnPagePlaceholder {
    private final float percent;
    private final int gapRows;

    public TurnPagePercentWidget(float percent) {
        this(percent, 0);
    }

    public TurnPagePercentWidget(float percent, int gapRows) {
        super(1, 1);
        this.percent = percent;
        this.gapRows = gapRows;
    }

    @Override
    public float getPercent() { return percent; }

    @Override
    public int getGapRows() { return gapRows; }
}
