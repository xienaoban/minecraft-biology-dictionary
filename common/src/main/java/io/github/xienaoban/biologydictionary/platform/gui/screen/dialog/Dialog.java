package io.github.xienaoban.biologydictionary.platform.gui.screen.dialog;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.sounds.SoundEvents;

/**
 * Base of modal dialogs. Covers the whole screen so it captures every
 * hover/click, and paints a translucent backdrop. Subclasses populate
 * {@link #panelBox} in their {@link #onResize} and override {@link #onRender}
 * to draw the panel — clicks inside {@code panelBox} are swallowed, clicks on
 * the backdrop dismiss the dialog.
 */
@ClientOnly
public class Dialog extends ScreenElement {
    public static final float DIALOG_PRIORITY = 100;

    private static final int BACKDROP_COLOR = 0xAA000000;

    /** Panel bounds; subclasses set this in onResize. */
    protected final ScreenElementBox panelBox = new ScreenElementBox();

    public Dialog() {
        super(true, true);
        setPriority(DIALOG_PRIORITY);
    }

    /** Detach from the screen. */
    public void close() {
        setParent(null);
    }

    @Override
    protected void onResize(int width, int height) {
        getBox().set(0, 0, width, height);
    }

    @Override
    protected boolean onMouseDown(float mouseX, float mouseY, int button) {
        if (panelBox.isInBox(mouseX, mouseY)) {
            return true;  // swallow clicks on the panel's blank area
        }
        if (isMouseLeft(button)) {
            ClientUtils.playScreenSound(SoundEvents.WOODEN_BUTTON_CLICK_OFF, 1.0F, 0.8F);
            close();
        }
        return true;  // modal: swallow every backdrop click so nothing leaks through
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ScreenElementBox box = getBox();
        ctx.renderRectangle(BACKDROP_COLOR, ctx.getZ(),
                box.getLeft(), box.getTop(), box.getRight(), box.getBottom());
    }

    @Override
    protected float getZOffset() { return 200; }
}
