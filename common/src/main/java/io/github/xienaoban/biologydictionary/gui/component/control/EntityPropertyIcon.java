package io.github.xienaoban.biologydictionary.gui.component.control;

import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.TextureInfo;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;

@ClientOnly
public class EntityPropertyIcon extends ScreenElement {
    private final TextureInfo texture;
    private final float textureLeft, textureTop;

    public EntityPropertyIcon(TextureInfo texture, float textureLeft, float textureTop) {
        this.texture = texture;
        this.textureLeft = textureLeft;
        this.textureTop = textureTop;

        getBox().setSize(Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);
        setSelectable(false);
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ctx.renderTexture(texture, textureLeft, textureTop, ctx.getZ(), getBox().getLeft(), getBox().getTop(), getBox().getWidth(), getBox().getHeight());
    }
}
