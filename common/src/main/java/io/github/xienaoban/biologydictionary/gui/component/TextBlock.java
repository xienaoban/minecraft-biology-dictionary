package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenConsts;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.FontUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * An immutable standalone text block. Supports scale, horizontal and vertical
 * alignment, line spacing and padding, optional line splitting (multi-line)
 * with ellipsis and a line limit
 * ({@code >0} = hard limit, {@code 0} = fit the content height, {@code -1} = no limit),
 * and an optional "show full text on hover" tooltip when the text is clipped.
 * <p>
 * Ellipsis behaviour mirrors {@code EntityDescriptionWidget} (multi-line) and
 * {@code EntityCardWidget} (single-line {@link FontUtils#truncateByWidth}).
 */
@ClientOnly
public class TextBlock extends ScreenElement implements ScreenConsts {
    private static final FormattedCharSequence ELLIPSIS = Component.literal("...").getVisualOrderText();
    private static final HorizontalAlignment DEFAULT_HORIZONTAL_ALIGNMENT = HorizontalAlignment.LEFT;
    private static final VerticalAlignment DEFAULT_VERTICAL_ALIGNMENT = VerticalAlignment.CENTER;
    private static final float DEFAULT_LINE_SPACING = 0F;
    private static final float DEFAULT_PADDING = 0F;
    private static final boolean DEFAULT_SPLIT_LINES = false;
    private static final boolean DEFAULT_ELLIPSIS = false;
    private static final int DEFAULT_MAX_LINES = 0;
    private static final boolean DEFAULT_SHOW_FULL_ON_HOVER = false;

    public enum HorizontalAlignment { LEFT, CENTER, RIGHT }

    public enum VerticalAlignment { TOP, CENTER, BOTTOM }

    public static final class Builder {
        private Component text = TextUtils.empty();
        private float scale = 0.5F;
        private int color = Colors.BLACK;
        private boolean splitLines = DEFAULT_SPLIT_LINES;
        private boolean ellipsis = DEFAULT_ELLIPSIS;
        private int maxLines = DEFAULT_MAX_LINES;
        private HorizontalAlignment horizontalAlignment = DEFAULT_HORIZONTAL_ALIGNMENT;
        private VerticalAlignment verticalAlignment = DEFAULT_VERTICAL_ALIGNMENT;
        private float lineSpacing = DEFAULT_LINE_SPACING;
        private float paddingLeft = DEFAULT_PADDING;
        private float paddingTop = DEFAULT_PADDING;
        private float paddingRight = DEFAULT_PADDING;
        private float paddingBottom = DEFAULT_PADDING;
        private boolean showFullOnHover = DEFAULT_SHOW_FULL_ON_HOVER;

        private Builder() {}

        public Builder text(Component text) {
            this.text = (text == null) ? TextUtils.empty() : text;
            return this;
        }

        public Builder scale(float scale) { this.scale = scale; return this; }
        public Builder color(int color) { this.color = color; return this; }
        public Builder splitLines(boolean splitLines) { this.splitLines = splitLines; return this; }
        public Builder ellipsis(boolean ellipsis) { this.ellipsis = ellipsis; return this; }
        public Builder maxLines(int maxLines) { this.maxLines = maxLines; return this; }
        public Builder horizontalAlignment(HorizontalAlignment horizontalAlignment) { this.horizontalAlignment = horizontalAlignment; return this; }
        public Builder verticalAlignment(VerticalAlignment verticalAlignment) { this.verticalAlignment = verticalAlignment; return this; }
        public Builder lineSpacing(float lineSpacing) { this.lineSpacing = lineSpacing; return this; }
        public Builder padding(float padding) { return padding(padding, padding, padding, padding); }
        public Builder padding(float left, float top, float right, float bottom) {
            this.paddingLeft = left;
            this.paddingTop = top;
            this.paddingRight = right;
            this.paddingBottom = bottom;
            return this;
        }
        public Builder showFullOnHover(boolean showFullOnHover) { this.showFullOnHover = showFullOnHover; return this; }

        public TextBlock build() {
            return new TextBlock(text, scale, color,
                    splitLines, ellipsis, maxLines,
                    horizontalAlignment, verticalAlignment,
                    lineSpacing, paddingLeft, paddingTop, paddingRight, paddingBottom,
                    showFullOnHover);
        }
    }

    public static Builder create() {
        return new Builder();
    }

    private final Component text;
    private final float scale;
    private final int color;
    private final boolean splitLines;
    private final boolean ellipsis;
    private final int maxLines;
    private final HorizontalAlignment horizontalAlignment;
    private final VerticalAlignment verticalAlignment;
    private final float lineSpacing;
    private final float paddingLeft;
    private final float paddingTop;
    private final float paddingRight;
    private final float paddingBottom;
    private final boolean showFullOnHover;

    private boolean truncated = false;

    // The final render result (wrapped lines, ellipsis line) is recomputed only
    // when the box size changes, since the config is immutable.
    private int cachedBoxWidth = -1;
    private int cachedBoxHeight = -1;
    private List<FormattedCharSequence> cachedRenderLines = List.of();

    public TextBlock(Component text, float scale, int color) {
        this(text, scale, color, DEFAULT_HORIZONTAL_ALIGNMENT);
    }

    public TextBlock(Component text, float scale, int color, HorizontalAlignment horizontalAlignment) {
        this(text, scale, color,
                DEFAULT_SPLIT_LINES, DEFAULT_ELLIPSIS, DEFAULT_MAX_LINES,
                horizontalAlignment, DEFAULT_VERTICAL_ALIGNMENT,
                DEFAULT_LINE_SPACING, DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING,
                DEFAULT_SHOW_FULL_ON_HOVER);
    }

    private TextBlock(Component text, float scale, int color,
                      boolean splitLines, boolean ellipsis, int maxLines,
                      HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment,
                      float lineSpacing, float paddingLeft, float paddingTop, float paddingRight, float paddingBottom,
                      boolean showFullOnHover) {
        super(true, false);
        this.text = (text == null) ? TextUtils.empty() : text;
        this.scale = scale;
        this.color = color;
        this.splitLines = splitLines;
        this.ellipsis = ellipsis;
        this.maxLines = maxLines;
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        this.lineSpacing = lineSpacing;
        this.paddingLeft = paddingLeft;
        this.paddingTop = paddingTop;
        this.paddingRight = paddingRight;
        this.paddingBottom = paddingBottom;
        this.showFullOnHover = showFullOnHover;
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        if (text.getString().isEmpty()) {
            truncated = false;
            return;
        }
        ensureCached();
        float lineHeight = FontUtils.getLineHeight(FontUtils.getGlobalFont(), scale);
        float y = firstLineY(lineHeight);
        for (FormattedCharSequence line : cachedRenderLines) {
            renderLine(ctx, line, y);
            y += lineHeight + lineSpacing;
        }
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        if (!showFullOnHover || !truncated || text.getString().isEmpty()) { return false; }
        int maxTextWidth = Math.max(1, (int)(contentWidth() / scale));
        List<FormattedCharSequence> allLines = FontUtils.toLines(text, FontUtils.getGlobalFont(), maxTextWidth);
        float lineHeight = FontUtils.getLineHeight(FontUtils.getGlobalFont(), scale);
        // Align the first line with the visible render, like EntityDescriptionWidget
        float firstLineY = firstLineY(lineHeight);
        ctx.renderLinedTooltipCentered(allLines, scale,
                (getBox().getLeft() + getBox().getRight()) / 2F, firstLineY - 2);
        return true;
    }

    private void ensureCached() {
        int width = (int) getBox().getWidth();
        int height = (int) getBox().getHeight();
        if (width == cachedBoxWidth && height == cachedBoxHeight) { return; }
        cachedBoxWidth = width;
        cachedBoxHeight = height;

        if (!splitLines) {
            Component display = ellipsis
                    ? FontUtils.truncateByWidth(text, FontUtils.getGlobalFont(), contentWidth(), scale)
                    : text;
            truncated = ellipsis && !display.getString().equals(text.getString());
            cachedRenderLines = List.of(display.getVisualOrderText());
            return;
        }

        float lineHeight = FontUtils.getLineHeight(FontUtils.getGlobalFont(), scale);
        int maxTextWidth = Math.max(1, (int)(contentWidth() / scale));
        List<FormattedCharSequence> allLines = FontUtils.toLines(text, FontUtils.getGlobalFont(), maxTextWidth);
        int maxVisibleLines = Math.max(1, (int)((contentHeight() + lineSpacing) / (lineHeight + lineSpacing)));
        int lineLimit;
        if (maxLines > 0) { lineLimit = maxLines; }
        else if (maxLines == 0) { lineLimit = maxVisibleLines; }
        else { lineLimit = allLines.size(); }
        int displayCount = Math.min(allLines.size(), lineLimit);
        truncated = allLines.size() > displayCount;
        if (truncated && ellipsis) {
            // "..." takes one line, so it counts towards the line limit
            displayCount = Math.max(0, lineLimit - 1);
            List<FormattedCharSequence> lines = new ArrayList<>(displayCount + 1);
            lines.addAll(allLines.subList(0, displayCount));
            lines.add(ELLIPSIS);
            cachedRenderLines = List.copyOf(lines);
        } else {
            cachedRenderLines = List.copyOf(allLines.subList(0, displayCount));
        }
    }

    private float firstLineY(float lineHeight) {
        int lineCount = cachedRenderLines.size();
        float totalHeight = lineCount * lineHeight + (lineCount - 1) * lineSpacing;
        float offset = switch (verticalAlignment) {
            case TOP -> 0F;
            case CENTER -> (contentHeight() - totalHeight) / 2F;
            case BOTTOM -> contentHeight() - totalHeight;
        };
        // Fall back to the top when the lines do not fit the content area
        return contentTop() + Math.max(0F, offset);
    }

    private float contentLeft() { return getBox().getLeft() + paddingLeft; }
    private float contentTop() { return getBox().getTop() + paddingTop; }
    private float contentRight() { return getBox().getRight() - paddingRight; }
    private float contentWidth() { return getBox().getWidth() - paddingLeft - paddingRight; }
    private float contentHeight() { return getBox().getHeight() - paddingTop - paddingBottom; }

    private void renderLine(ScreenRenderingContext ctx, FormattedCharSequence line, float y) {
        float x;
        switch (horizontalAlignment) {
            case CENTER -> x = (contentLeft() + contentRight()) / 2F - ctx.calcTextWidth(line) * scale / 2F;
            case RIGHT -> x = contentRight() - ctx.calcTextWidth(line) * scale;
            default -> x = contentLeft();
        }
        ctx.renderText(line, color, scale, ctx.getZ(), x, y);
    }
}
