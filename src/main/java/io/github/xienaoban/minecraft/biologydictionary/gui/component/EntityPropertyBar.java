package io.github.xienaoban.minecraft.biologydictionary.gui.component;

import io.github.xienaoban.minecraft.biologydictionary.platform.gui.TextureInfo;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class EntityPropertyBar extends ScreenElement {
    private final TextureInfo texture;
    private final float textureLeft, textureTop;

    private float percent;
    private Component text;

    public EntityPropertyBar(TextureInfo texture, float textureLeft, float textureTop) {
        this.texture = texture;
        this.textureLeft = textureLeft;
        this.textureTop = textureTop + 1;
        this.percent = 0;
        this.text = Component.empty();

        getBox().setSize(Widget.calcWidth(Page.COLUMNS / 2) - Widget.WIDGET_WIDTH - 1, Widget.WIDGET_HEIGHT - 2);
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        renderBar(ctx, 1.0F, true);
        renderBar(ctx, percent, false);
        renderText(ctx);
    }

    /**
     * leftPattern: 5px, midPattern: 10px * n, rightPattern: 5px
     */
    private void renderBar(ScreenRenderingContext ctx, float percent, boolean isBackground) {
        if (percent == 0) return;
        float textureLeft = this.textureLeft + (isBackground ? 0 : Widget.WIDGET_WIDTH * 2);
        float width = percent * getBox().getWidth();
        float currPos = 0;
        float widthLeft = Math.min(width, 5);
        ctx.renderTexture(texture, textureLeft, textureTop, ctx.getZ(), getBox().getLeft(), getBox().getTop(), widthLeft, getBox().getHeight());
        currPos += widthLeft;
        if (currPos == width) return;

        float widthMid = Math.min(width - 5, getBox().getWidth() - 10);
        while (widthMid > 10) {
            ctx.renderTexture(texture, textureLeft + 5, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop(), 10, getBox().getHeight());
            currPos += 10;
            widthMid -= 10;
        }
        ctx.renderTexture(texture, textureLeft + 5, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop(), widthMid, getBox().getHeight());
        currPos += widthMid;
        if (currPos == width) return;

        float widthRight = width - (getBox().getWidth() - 5);
        ctx.renderTexture(texture, textureLeft + 15, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop(), widthRight, getBox().getHeight());
    }

    private void renderText(ScreenRenderingContext ctx) {
        ctx.renderText(text, 0xBBFFFFFF, 0.5F, getBox().getLeft() + 3.0F, getBox().getTop() + 2.25F);
    }

    public void updatePercent(float zeroToOne) {
        this.percent = zeroToOne;
    }

    public void updateText(Component text) {
        this.text = text;
    }
}
