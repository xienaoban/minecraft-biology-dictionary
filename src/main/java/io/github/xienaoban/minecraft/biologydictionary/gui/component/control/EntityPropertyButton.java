package io.github.xienaoban.minecraft.biologydictionary.gui.component.control;

import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.TextureInfo;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenElement;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenRenderingContext;

public class EntityPropertyButton extends ScreenElement {
    private final TextureInfo texture;
    private final float textureLeft, textureTop;
    private float textureLeftOffset;

    public EntityPropertyButton(TextureInfo texture, float textureLeft, float textureTop) {
        this.texture = texture;
        this.textureLeft = textureLeft;
        this.textureTop = textureTop;
        this.textureLeftOffset = 0;

        getBox().setSize(Widget.WIDGET_WIDTH - 2, Widget.WIDGET_HEIGHT - 2);
    }

    public final void setTextureLeftOffset(float textureLeftOffset) {
        this.textureLeftOffset = textureLeftOffset;
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ctx.renderTexture(texture, textureLeft + textureLeftOffset, textureTop, ctx.getZ(), getBox().getLeft() - 1, getBox().getTop() - 1, getBox().getWidth() + 2, getBox().getHeight() + 2);
    }
}
