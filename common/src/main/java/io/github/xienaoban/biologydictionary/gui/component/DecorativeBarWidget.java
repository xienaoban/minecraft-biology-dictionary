package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;

/**
 * The small decorative bar at the bottom of the right page of an entity list.
 */
@ClientOnly
public final class DecorativeBarWidget extends Widget {
    private static final int BAR_WIDTH = 6;

    private final boolean filled;

    public DecorativeBarWidget(boolean filled) {
        super(1, Page.COLUMNS);
        this.filled = filled;
        setHoverable(false);
        setSelectable(false);
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ScreenElementBox box = getBox();
        float barLeft = box.getLeft() + (box.getWidth() - BAR_WIDTH) / 2F;
        float barTop = box.getTop();
        float barBottom = box.getBottom();
        float z = ctx.getZ();
        int texY = filled ? 230 : 240;
        int texYEnd = texY + 10;
        ctx.renderTexture(Textures.ICONS, 150, texY, 152, texYEnd, z, barLeft, barTop, barLeft + 2, barBottom);
        ctx.renderTexture(Textures.ICONS, 152, texY, 154, texYEnd, z, barLeft + 2, barTop, barLeft + 4, barBottom);
        ctx.renderTexture(Textures.ICONS, 188, texY, 190, texYEnd, z, barLeft + 4, barTop, barLeft + 6, barBottom);
    }
}
