package io.github.xienaoban.minecraft.biologydictionary.gui.component;

import io.github.xienaoban.minecraft.biologydictionary.gui.util.TextureInfo;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class EntityPropertyBar extends ScreenElement {
    private final TextureInfo texture;
    private final float left, top;

    public EntityPropertyBar(TextureInfo texture, float textureLeft, float textureTop) {
        this.texture = texture;
        this.left = textureLeft;
        this.top = textureTop;
        getBox().setSize(Widget.calcWidth(Page.COLUMNS / 2) - Widget.WIDGET_WIDTH - 1, Widget.WIDGET_HEIGHT);
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ctx.renderTexture(texture, left, top, ctx.getZ(), getBox().getLeft(), getBox().getTop(), getBox().getWidth(), getBox().getHeight());
    }

    protected void renderText(ScreenRenderingContext ctx, Component text) {
        ctx.renderText(text, 0xFFFFFFFF, 0.5F, getBox().getLeft() + 3, getBox().getTop() + 3.25F);
    }
}
