package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.minecraft.network.chat.Component;

/**
 * The base of the bookmark column on the left of the book. Each screen defines the
 * bookmarks it needs so that they can reach the state of that screen.
 */
@ClientOnly
public abstract class Bookmark extends ScreenElement {
    private static final int L = 1, T = 24;
    private static final int W = 3, H = 1;
    private static final int CNT = 5;

    private final Component text;

    private int idx;

    public Bookmark(Component text) {
        this.text = text;
        getBox().setSize(W * Widget.WIDGET_WIDTH, H * Widget.WIDGET_HEIGHT);
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ScreenElementBox box = getBox();
        float offsetL = (ctx.getElementScreen().getHoveredElement() == this ? W : 0);
        float offsetT = (idx % CNT) * H;
        ctx.renderTexture(Textures.ICONS,
                (L + offsetL) * Widget.WIDGET_WIDTH,
                (T - offsetT) * Widget.WIDGET_HEIGHT,
                ctx.getZ(), box.getLeft(), box.getTop(), box.getWidth(), box.getHeight());
        ctx.renderRightAlignedText(text, Colors.COMMON_LIGHT_TEXT, 0.5F, ctx.getZ(),
                box.getRight() - 2F, box.getTop() + 2.5F);
    }
}
