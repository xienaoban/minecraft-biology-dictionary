package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenConsts;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.minecraft.network.chat.Component;

/**
 * The base row of a category tree: one indented line of text with its own background
 * strip. Each screen defines its own subclass with the behaviour of that tree (list the
 * entities, select for the search filter, ...).
 */
@ClientOnly
public abstract class Catalog extends Widget implements ScreenConsts {
    protected final int depth;
    protected final Component text;

    public Catalog(int depth, Component text) {
        super(1, Page.COLUMNS);
        this.depth = depth;
        this.text = text;
    }

    /**
     * Whether this row is currently selected; only the search screen has a selection.
     */
    protected boolean isSelected() {
        return false;
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ScreenElementBox box = getBox();
        int d = depth * 8;
        ctx.renderTexture(Textures.ICONS, 7 * Widget.WIDGET_WIDTH, 24 * Widget.WIDGET_HEIGHT,
                ctx.getZ(),
                box.getLeft() + d, box.getTop(),
                4 * Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);
        if (isSelected()) {
            ctx.renderRectangle(0x33AF711F, 1F, ctx.getZ(),
                    box.getLeft() + d, box.getTop(), box.getRight(), box.getBottom());
        }
        boolean hovered = ctx.getElementScreen().getHoveredElement() == this;
        int textColor, lineColor;
        if (hovered) {
            textColor = Colors.BLACK;
            lineColor = 0x44AF711F;
        } else {
            textColor = Colors.COMMON_DARK_LIGHTER_TEXT;
            lineColor = 0x22AF711F;
        }
        if (depth != 0) {
            ctx.renderHorizontalLine(lineColor, 1F, ctx.getZ(), (box.getTop() + box.getBottom()) / 2,
                    box.getLeft() + 1, box.getLeft() + d - 1);
        }
        ctx.renderText(text, textColor, 0.5F, ctx.getZ(), box.getLeft() + d + 12, box.getTop() + 3 + TXT_TO);
    }
}
