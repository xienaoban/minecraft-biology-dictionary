package io.github.xienaoban.minecraft.biologydictionary.gui.component.control;

import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.TextureInfo;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.minecraft.network.chat.Component;

public class EntityPropertyBar extends ScreenElement {
    private final TextureInfo texture;
    private final float textureLeft, textureTop;

    public EntityPropertyBar(TextureInfo texture, float textureLeft, float textureTop) {
        this.texture = texture;
        this.textureLeft = textureLeft;
        this.textureTop = textureTop;

        getBox().setSize(Widget.WIDGET_WIDTH * 2, Widget.WIDGET_HEIGHT - 2);
        setSelectable(false);
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
    }

    /**
     * leftPattern: 5px, midPattern: 10px * n, rightPattern: 5px
     */
    protected void renderFullBar(ScreenRenderingContext ctx) {
        float currPos = 0;
        float widthLeft = 5;
        ctx.renderTexture(texture, textureLeft, textureTop, ctx.getZ(), getBox().getLeft(), getBox().getTop() - 1, widthLeft, getBox().getHeight() + 2);
        currPos += widthLeft;

        float widthMid = getBox().getWidth() - 10;
        while (widthMid > 10) {
            ctx.renderTexture(texture, textureLeft + 5, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop() - 1, 10, getBox().getHeight() + 2);
            currPos += 10;
            widthMid -= 10;
        }
        ctx.renderTexture(texture, textureLeft + 5, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop() - 1, widthMid, getBox().getHeight() + 2);
        currPos += widthMid;

        float widthRight = 5;
        ctx.renderTexture(texture, textureLeft + 15, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop() - 1, widthRight, getBox().getHeight() + 2);
    }

    /**
     * leftPattern: 5px, midPattern: 10px * n, rightPattern: 5px
     */
    protected void renderProgressBar(ScreenRenderingContext ctx, float percent) {
        if (percent == 0) return;
        float width = percent * getBox().getWidth();
        float currPos = 0;
        float widthLeft = Math.min(width, 5);
        float textureLeft = this.textureLeft + 2 * Widget.WIDGET_WIDTH;
        ctx.renderTexture(texture, textureLeft, textureTop, ctx.getZ(), getBox().getLeft(), getBox().getTop() - 1, widthLeft, getBox().getHeight() + 2);
        currPos += widthLeft;
        if (currPos == width) return;

        float widthMid = Math.min(width - 5, getBox().getWidth() - 10);
        while (widthMid > 10) {
            ctx.renderTexture(texture, textureLeft + 5, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop() - 1, 10, getBox().getHeight() + 2);
            currPos += 10;
            widthMid -= 10;
        }
        ctx.renderTexture(texture, textureLeft + 5, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop() - 1, widthMid, getBox().getHeight() + 2);
        currPos += widthMid;
        if (currPos == width) return;

        float widthRight = width - (getBox().getWidth() - 5);
        ctx.renderTexture(texture, textureLeft + 15, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop() - 1, widthRight, getBox().getHeight() + 2);
    }

    protected void renderInnerText(ScreenRenderingContext ctx, Component text) {
        renderInnerText(ctx, text, 0xBBFFFFFF);
    }

    protected void renderInnerText(ScreenRenderingContext ctx, Component text, int color) {
        ctx.renderText(text, color, 0.5F, getBox().getLeft() + 3.0F, getBox().getTop() + 2.25F);
    }
}
