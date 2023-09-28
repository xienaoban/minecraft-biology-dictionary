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

//    protected void renderText(ScreenRenderingContext ctx) {
//        ctx.renderText(text, 0xBBFFFFFF, 0.5F, getBox().getLeft() + 3.0F, getBox().getTop() + 2.25F);
//    }
}
