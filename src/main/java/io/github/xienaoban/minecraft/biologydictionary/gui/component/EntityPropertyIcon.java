package io.github.xienaoban.minecraft.biologydictionary.gui.component;

import io.github.xienaoban.minecraft.biologydictionary.gui.util.TextureInfo;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EntityPropertyIcon extends ScreenElement {
    private final TextureInfo texture;
    private final float left, top;

    public EntityPropertyIcon(TextureInfo texture, float textureLeft, float textureTop) {
        this.texture = texture;
        this.left = textureLeft;
        this.top = textureTop;
        getBox().setSize(Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ctx.renderTexture(texture, left, top, ctx.getZ(), getBox().getLeft(), getBox().getTop(), getBox().getWidth(), getBox().getHeight());
    }
}
