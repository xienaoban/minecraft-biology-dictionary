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
     * leftPattern: 1px + 5px, midPattern: 8px * n, rightPattern: 5px + 1px
     */
    protected void renderFullBar(ScreenRenderingContext ctx) {
        float currPos = 0;
        float widthLeft = 5;
        ctx.renderTexture(texture, textureLeft, textureTop, ctx.getZ(), getBox().getLeft() - 1, getBox().getTop() - 1, widthLeft + 1, getBox().getHeight() + 2);
        currPos += widthLeft;

        float widthMid = getBox().getWidth() - 10;
        while (widthMid > 8) {
            ctx.renderTexture(texture, textureLeft + 6, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop() - 1, 8, getBox().getHeight() + 2);
            currPos += 8;
            widthMid -= 8;
        }
        ctx.renderTexture(texture, textureLeft + 6, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop() - 1, widthMid, getBox().getHeight() + 2);
        currPos += widthMid;

        float widthRight = 5;
        ctx.renderTexture(texture, textureLeft + 14, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop() - 1, widthRight + 1, getBox().getHeight() + 2);
    }

    /**
     * leftPattern: 1px + 5px, midPattern: 8px * n, rightPattern: 5px + 1px
     */
    protected void renderProgressBar(ScreenRenderingContext ctx, float percent) {
        if (percent <= 0) return;
        if (percent > 1) percent = 1;
        float width = percent * getBox().getWidth();
        float currPos = 0;
        float widthLeft = Math.min(width, 5);
        float textureLeft = this.textureLeft + 2 * Widget.WIDGET_WIDTH;
        ctx.renderTexture(texture, textureLeft, textureTop, ctx.getZ(), getBox().getLeft() - 1, getBox().getTop() - 1, widthLeft + 1, getBox().getHeight() + 2);
        currPos += widthLeft;
        if (currPos == width) return;

        float widthMid = Math.min(width - 5, getBox().getWidth() - 10);
        while (widthMid > 8) {
            ctx.renderTexture(texture, textureLeft + 6, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop() - 1, 8, getBox().getHeight() + 2);
            currPos += 8;
            widthMid -= 8;
        }
        ctx.renderTexture(texture, textureLeft + 6, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop() - 1, widthMid, getBox().getHeight() + 2);
        currPos += widthMid;
        if (currPos == width) return;

        float widthRight = width - (getBox().getWidth() - 5);
        ctx.renderTexture(texture, textureLeft + 14, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop() - 1, widthRight + ((int) percent), getBox().getHeight() + 2);
    }

    protected void renderInnerText(ScreenRenderingContext ctx, Component text) {
        renderInnerText(ctx, text, 0xBBFFFFFF);
    }

    protected void renderInnerText(ScreenRenderingContext ctx, Component text, int color) {
        ctx.renderText(text, color, 0.5F, getBox().getLeft() + 3.0F, getBox().getTop() + 2.25F);
    }
}
