package io.github.xienaoban.biologydictionary.gui.component.entitylist;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * The discovery progress bar of an entity list, showing how many of the listed entity
 * types are discovered.
 */
@ClientOnly
public final class DiscoveryProgressWidget extends Widget {
    private static final int BAR_LEFT_CAP = 2;
    private static final int BAR_TILE = 36;
    private static final int BAR_TILE_COUNT = 2;
    private static final int BAR_RIGHT_CAP = 2;
    private static final int BAR_WIDTH = BAR_LEFT_CAP + BAR_TILE * BAR_TILE_COUNT + BAR_RIGHT_CAP;

    private final int total;
    private final int discovered;

    public DiscoveryProgressWidget(int total, int discovered) {
        super(1, Page.COLUMNS);
        this.total = total;
        this.discovered = discovered;
        setSelectable(false);
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ScreenElementBox box = getBox();
        float barLeft = box.getLeft() + (box.getWidth() - BAR_WIDTH) / 2F;
        float barTop = box.getTop();
        float barBottom = box.getBottom();
        float z = ctx.getZ();
        float x = barLeft;

        // Background
        ctx.renderTexture(Textures.ICONS, 150, 240, 152, 250, z, x, barTop, x + BAR_LEFT_CAP, barBottom);
        x += BAR_LEFT_CAP;
        for (int i = 0; i < BAR_TILE_COUNT; i++) {
            ctx.renderTexture(Textures.ICONS, 152, 240, 188, 250, z, x, barTop, x + BAR_TILE, barBottom);
            x += BAR_TILE;
        }
        ctx.renderTexture(Textures.ICONS, 188, 240, 190, 250, z, x, barTop, x + BAR_RIGHT_CAP, barBottom);

        // Foreground
        if (discovered > 0) {
            float progress = (float) discovered / total;
            float totalProgressWidth = (float) BAR_TILE * BAR_TILE_COUNT * progress;
            x = barLeft;
            ctx.renderTexture(Textures.ICONS, 150, 230, 152, 240, z, x, barTop, x + BAR_LEFT_CAP, barBottom);
            x += BAR_LEFT_CAP;
            float remaining = totalProgressWidth;
            for (int i = 0; i < BAR_TILE_COUNT && remaining > 0; i++) {
                float w = Math.min(remaining, BAR_TILE);
                ctx.renderTexture(Textures.ICONS, 152, 230, 152 + w, 240, z, x, barTop, x + w, barBottom);
                x += w;
                remaining -= w;
            }
            ctx.renderTexture(Textures.ICONS, 188, 230, 190, 240, z, x, barTop, x + BAR_RIGHT_CAP, barBottom);
        }

        Component text = TextUtils.literal(discovered + "/" + total);
        int color = discovered * 2 < total ? Colors.COMMON_DARK_LIGHTER_TEXT : Colors.COMMON_LIGHT_TEXT;
        ctx.renderCenteredText(text, color, 0.5F, ctx.getZ(),
                box.getLeft() + box.getWidth() / 2F, barTop + 3.25F);
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        ScreenElementBox box = getBox();
        float midX = (box.getLeft() + box.getRight()) / 2;
        ctx.renderComponentTooltipCentered(
                List.of(TextUtils.translate(Lang.WIDGET_DISCOVERY_PROGRESS)),
                0.5F, midX, box.getBottom() + 1);
        return true;
    }
}
