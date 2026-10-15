package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.sounds.SoundEvents;

import java.util.List;

/**
 * Enters or leaves the selection mode of an entity list.
 */
@ClientOnly
public final class EntitySelectionModeButton extends Widget {
    private static final int ICON_ROW = 23;
    private static final int ICON_ENTER_COLUMN = 23;
    private static final int ICON_EXIT_COLUMN = 24;

    private final boolean exit;
    private final Runnable action;

    public EntitySelectionModeButton(boolean exit, Runnable action) {
        super(1, 1);
        this.exit = exit;
        this.action = action;
    }

    @Override
    protected boolean onMouseDown(float mouseX, float mouseY, int button) {
        if (!isMouseLeft(button)) { return super.onMouseDown(mouseX, mouseY, button); }
        ClientUtils.playScreenSound(
                exit ? SoundEvents.WOODEN_BUTTON_CLICK_OFF : SoundEvents.WOODEN_BUTTON_CLICK_ON,
                1.0F, 0.8F);
        action.run();
        return true;
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ScreenElementBox box = getBox();
        int iconColumn = exit ? ICON_EXIT_COLUMN : ICON_ENTER_COLUMN;
        ctx.renderTexture(Textures.ICONS,
                iconColumn * WIDGET_WIDTH, ICON_ROW * WIDGET_HEIGHT,
                ctx.getZ(), box.getLeft(), box.getTop(), box.getWidth(), box.getHeight());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        ScreenElementBox box = getBox();
        ctx.renderComponentTooltipCentered(
                List.of(TextUtils.translate(exit
                        ? Lang.WIDGET_ENTITY_SELECTION_EXIT_DESC
                        : Lang.WIDGET_ENTITY_SELECTION_ENTER_DESC)),
                0.5F, (box.getLeft() + box.getRight()) / 2, box.getBottom() + 1);
        return true;
    }
}
