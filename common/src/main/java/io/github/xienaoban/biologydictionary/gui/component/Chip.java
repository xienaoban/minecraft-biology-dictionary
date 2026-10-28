package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * A small dismissible label, the usual "filter chip" of a front end. Clicking it removes
 * itself from the owner; the background is a flat placeholder until the art is made.
 */
@ClientOnly
public class Chip extends ScreenElement {
    private final Component label;
    private final float textSize;
    private final Runnable onDismiss;

    /**
     * @param textSize font size in the scaled coordinate system; a smaller value keeps the
     *                 chip visually smaller than the buttons around it
     */
    public Chip(Component label, float textSize, Runnable onDismiss) {
        super(true, true);
        this.label = label;
        this.textSize = textSize;
        this.onDismiss = onDismiss;
    }

    public Component getLabel() {
        return label;
    }

    @Override
    protected boolean onMouseDown(float mouseX, float mouseY, int button) {
        if (!isMouseLeft(button)) { return false; }
        ClientUtils.playScreenSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 1.6F);
        onDismiss.run();
        return true;
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ScreenElementBox box = getBox();
        boolean hovered = ctx.getElementScreen().getHoveredElement() == this;
        ctx.renderRectangle(hovered ? 0x55AF711F : 0x33AF711F, ctx.getZ(),
                box.getLeft(), box.getTop(), box.getRight(), box.getBottom());
        ctx.renderCenteredText(label, Colors.COMMON_DARK_TEXT, textSize, ctx.getZ(),
                (box.getLeft() + box.getRight()) / 2F,
                (box.getTop() + box.getBottom()) / 2F - 9F * textSize / 2F + 1F);
    }
}
