package io.github.xienaoban.minecraft.biologydictionary.gui.component.control;

import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.TextureInfo;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;

public class EntityPropertyBar extends ScreenElement {
    private final TextureInfo texture;
    private final float textureLeft, textureTop;
    private ScreenElement text;

    public EntityPropertyBar(TextureInfo texture, float textureLeft, float textureTop) {
        this.texture = texture;
        this.textureLeft = textureLeft;
        this.textureTop = textureTop;

        this.text = null;

        getBox().setSize(Widget.WIDGET_WIDTH * 2, Widget.WIDGET_HEIGHT - 2);
        setSelectable(false);
    }

    public ScreenElement getElementText() { return text; }
    public void setElementText(ScreenElement text) {
        if (text != null) text.setPriority(100);
        updateSubScreenElement(this.text, text);
        this.text = text;
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        if (text != null) {
            float left = getBox().getLeft() + 2;
            float top = getBox().getTop() + 1;
            float right = getBox().getRight() - 2;
            float bottom = top + text.getBox().getHeight();
            text.getBox().set(left, top, right, bottom);
        }
    }

    /**
     * leftPattern: 5px, midPattern: 10px * n, rightPattern: 5px
     */
    protected void renderFullBar(ScreenRenderingContext ctx) {
        float currPos = 0;
        float widthLeft = 5;
        ctx.renderTexture(texture, textureLeft, textureTop, ctx.getZ(), getBox().getLeft(), getBox().getTop(), widthLeft, getBox().getHeight());
        currPos += widthLeft;

        float widthMid = getBox().getWidth() - 10;
        while (widthMid > 10) {
            ctx.renderTexture(texture, textureLeft + 5, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop(), 10, getBox().getHeight());
            currPos += 10;
            widthMid -= 10;
        }
        ctx.renderTexture(texture, textureLeft + 5, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop(), widthMid, getBox().getHeight());
        currPos += widthMid;

        float widthRight = 5;
        ctx.renderTexture(texture, textureLeft + 15, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop(), widthRight, getBox().getHeight());
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
}
