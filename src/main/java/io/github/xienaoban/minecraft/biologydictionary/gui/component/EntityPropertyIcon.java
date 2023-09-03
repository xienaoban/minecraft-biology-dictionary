package io.github.xienaoban.minecraft.biologydictionary.gui.component;

import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class EntityPropertyIcon extends ScreenElement {
    private final ResourceLocation texture;
    private final float width, height;
    private final float left, top;

    public EntityPropertyIcon(ResourceLocation texture, float resourceWidth, float resourceHeight,
                              float textureLeft, float textureTop) {
        this.texture = texture;
        this.width = resourceWidth;
        this.height = resourceHeight;
        this.left = textureLeft;
        this.top = textureTop;
        getBox().setSize(Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ctx.getScreen().renderTexture(ctx, texture, width, height, left, top, ctx.getScreen().getZ(), getBox().getLeft(), getBox().getTop(), getBox().getWidth(), getBox().getHeight());
    }
}
