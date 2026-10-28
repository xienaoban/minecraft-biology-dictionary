package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenConsts;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.minecraft.network.chat.Component;

/**
 * One line of descriptive text spanning a whole page row.
 */
@ClientOnly
public final class DescriptionWidget extends Widget implements ScreenConsts {
    private final Component text;

    public DescriptionWidget(int rows, int columns, Component text) {
        super(rows, columns);
        this.text = text;
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ScreenElementBox box = getBox();
        ctx.renderText(text, Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(),
                box.getLeft() + 2, box.getTop() + 3 + TXT_TO);
    }
}
