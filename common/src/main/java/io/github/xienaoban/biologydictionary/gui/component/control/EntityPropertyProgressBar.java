package io.github.xienaoban.biologydictionary.gui.component.control;

import io.github.xienaoban.biologydictionary.platform.gui.TextureInfo;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;

public class EntityPropertyProgressBar extends EntityPropertyBar {
    private float percent;

    public EntityPropertyProgressBar(TextureInfo texture, float textureLeft, float textureTop) {
        super(texture, textureLeft, textureTop);
        percent = 0;
    }

    public final void updatePercent(float zeroToOne) {
        percent = Math.min(Math.max(zeroToOne, 0.0F), 1.0F);
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        renderFullBar(ctx);
        renderProgressBar(ctx, percent);
    }
}
