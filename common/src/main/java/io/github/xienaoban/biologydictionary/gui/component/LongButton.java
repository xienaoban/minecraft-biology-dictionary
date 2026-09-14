package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenConsts;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * A generic wide rectangular button with a centered label. Renders as a flat
 * colored rect (brightened on hover) — swap {@link #onRender} for a textured
 * variant when the art is ready.
 */
@ClientOnly
public class LongButton extends ScreenElement implements ScreenConsts {
    public static final int STYLE_NORMAL  = 0xFF3A3A3A;
    public static final int STYLE_CONFIRM = 0xFF3A6B3A;
    public static final int STYLE_CANCEL  = 0xFF6B3A3A;

    public static final float WIDTH  = 60;
    public static final float HEIGHT = 12;

    private Component label;
    private int color;
    private Runnable action;

    public LongButton(Component label, int color, Runnable action) {
        super(true, true);
        this.label = label;
        this.color = color;
        this.action = action;
        getBox().setSize(WIDTH, HEIGHT);
    }

    public void setLabel(Component label) { this.label = label; }
    public void setColor(int color) { this.color = color; }
    public void setAction(Runnable action) { this.action = action; }

    @Override
    protected boolean onMouseDown(float mouseX, float mouseY, int button) {
        if (!isMouseLeft(button)) { return false; }
        ClientUtils.playScreenSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
        if (action != null) {
            action.run();
        }
        return true;
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ScreenElementBox box = getBox();
        boolean hovered = (ctx.getElementScreen().getHoveredElement() == this);
        int bg = hovered ? brighten(color, 36) : color;
        ctx.renderRectangle(bg, ctx.getZ(), box.getLeft(), box.getTop(), box.getRight(), box.getBottom());
        ctx.renderCenteredText(label, Colors.WHITE, 0.5F, ctx.getZ(),
                (box.getLeft() + box.getRight()) / 2F, box.getTop() + 3 + TXT_TO);
    }

    private static int brighten(int color, int amount) {
        int r = Math.min(255, (color >> 16 & 0xFF) + amount);
        int g = Math.min(255, (color >> 8 & 0xFF) + amount);
        int b = Math.min(255, (color & 0xFF) + amount);
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
    }
}
