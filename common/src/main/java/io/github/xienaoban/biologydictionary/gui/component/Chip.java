package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * A small dismissible label, the usual "filter chip" of a front end. Clicking it removes
 * itself from the owner.
 */
@ClientOnly
public class Chip extends ScreenElement {
    private static final int BACKGROUND_X = 150;
    private static final int BACKGROUND_HOVER_X = 170;
    private static final int BACKGROUND_Y = 230;
    private static final int BACKGROUND_HEIGHT = 10;
    private static final int BACKGROUND_CAP = 2;
    private static final int BACKGROUND_TILE = 16;

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
        float z = ctx.getZ();
        float texLeft = hovered ? BACKGROUND_HOVER_X : BACKGROUND_X;
        // The art is one pixel larger than the chip on every side, so the outer ring (transparent
        // by default, but free for resource packs to fill) is drawn outside the clickable box.
        float left = box.getLeft() - 1;
        float right = box.getRight() + 1;
        float top = box.getTop() + (box.getHeight() - BACKGROUND_HEIGHT) / 2F;
        float bottom = top + BACKGROUND_HEIGHT;

        ctx.renderTexture(Textures.ICONS, texLeft, BACKGROUND_Y, texLeft + BACKGROUND_CAP, BACKGROUND_Y + BACKGROUND_HEIGHT,
                z, left, top, left + BACKGROUND_CAP, bottom);
        float x = left + BACKGROUND_CAP;
        float middleWidth = right - left - 2F * BACKGROUND_CAP;
        for (float drawn = 0; drawn < middleWidth; drawn += BACKGROUND_TILE) {
            float w = Math.min(BACKGROUND_TILE, middleWidth - drawn);
            ctx.renderTexture(Textures.ICONS, texLeft + BACKGROUND_CAP, BACKGROUND_Y,
                    texLeft + BACKGROUND_CAP + w, BACKGROUND_Y + BACKGROUND_HEIGHT,
                    z, x, top, x + w, bottom);
            x += w;
        }
        ctx.renderTexture(Textures.ICONS, texLeft + BACKGROUND_CAP + BACKGROUND_TILE, BACKGROUND_Y,
                texLeft + BACKGROUND_CAP + BACKGROUND_TILE + BACKGROUND_CAP, BACKGROUND_Y + BACKGROUND_HEIGHT,
                z, x, top, x + BACKGROUND_CAP, bottom);

        ctx.renderCenteredText(label, Colors.COMMON_LIGHT_TEXT, textSize, z,
                (box.getLeft() + box.getRight()) / 2F,
                (box.getTop() + box.getBottom()) / 2F - 9F * textSize / 2F + 0.25F);
    }
}
