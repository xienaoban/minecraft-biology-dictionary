package io.github.xienaoban.minecraft.biologydictionary.gui.component.control;

import io.github.xienaoban.minecraft.biologydictionary.platform.gui.TextureInfo;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;

public class EntityPropertyTextBar extends EntityPropertyBar {

    public EntityPropertyTextBar(TextureInfo texture, float textureLeft, float textureTop) {
        super(texture, textureLeft, textureTop);
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        renderFullBar(ctx);
    }
}
