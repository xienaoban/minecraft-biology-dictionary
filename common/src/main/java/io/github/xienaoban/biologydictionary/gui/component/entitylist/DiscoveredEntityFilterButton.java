package io.github.xienaoban.biologydictionary.gui.component.entitylist;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.sounds.SoundEvents;

import java.util.List;

/**
 * Toggles the shared "only discovered entities" filter of the entity lists and asks the
 * host screen to rebuild its cards afterwards.
 */
@ClientOnly
public final class DiscoveredEntityFilterButton extends Widget {
    private static final int ICON_ROW = 23;
    private static final int ICON_ALL_COLUMN = 21;
    private static final int ICON_DISCOVERED_COLUMN = 22;

    private final Runnable onToggle;

    public DiscoveredEntityFilterButton(Runnable onToggle) {
        super(1, 1);
        this.onToggle = onToggle;
    }

    @Override
    protected boolean onMouseDown(float mouseX, float mouseY, int button) {
        if (!isMouseLeft(button)) { return super.onMouseDown(mouseX, mouseY, button); }
        boolean showOnlyDiscovered = BiologyDictionaryClient.toggleShowOnlyDiscoveredEntities();
        ClientUtils.playScreenSound(
                showOnlyDiscovered ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.WOODEN_BUTTON_CLICK_OFF,
                1.0F, 0.8F);
        onToggle.run();
        return true;
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ScreenElementBox box = getBox();
        boolean showOnlyDiscovered = BiologyDictionaryClient.shouldShowOnlyDiscoveredEntities();
        int iconColumn = showOnlyDiscovered ? ICON_DISCOVERED_COLUMN : ICON_ALL_COLUMN;
        ctx.renderTexture(Textures.ICONS,
                iconColumn * WIDGET_WIDTH, ICON_ROW * WIDGET_HEIGHT,
                ctx.getZ(), box.getLeft(), box.getTop(), box.getWidth(), box.getHeight());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        ScreenElementBox box = getBox();
        boolean showOnlyDiscovered = BiologyDictionaryClient.shouldShowOnlyDiscoveredEntities();
        ctx.renderComponentTooltipCentered(
                List.of(TextUtils.translate(showOnlyDiscovered
                        ? Lang.WIDGET_ENTITY_FILTER_DISCOVERED_DESC
                        : Lang.WIDGET_ENTITY_FILTER_ALL_DESC)),
                0.5F, (box.getLeft() + box.getRight()) / 2, box.getBottom() + 1);
        return true;
    }
}
