package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import net.minecraft.network.chat.Component;

public class CenteredMessage extends ScreenElement {
    private static int alpha(long curr, long end) {
        long beginFade = end - 2000;
        if (curr <= beginFade) { return 0xFF; }
        return 0xFF * (int) (end - curr) / 2000;
    }

    private Component text = null;
    private int colorNoAlpha = 0;
    private long endTime = -1;

    public CenteredMessage() {
        setHoverable(true);
        setSelectable(false);
    }

    public void setText(Component text) {
        setText(text, 0x00FFFF00);
    }

    public void setText(Component text, int color) {
        this.text = text;
        this.colorNoAlpha = color;
        this.endTime = System.currentTimeMillis() + 5000;
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        if (text == null) { return; }

        long currTime = System.currentTimeMillis();
        if (currTime > endTime) { text = null; return; }

        if (isFocused(ctx.getMouseX(), ctx.getMouseY())) {
            this.endTime = Math.max(endTime, currTime + 3000);
        }

        int color = colorNoAlpha | (alpha(currTime, endTime) << 24);
        ctx.renderCenteredText(text, color, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, (getBox().getTop() + getBox().getBottom()) / 2 - 5);
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
    }
}
