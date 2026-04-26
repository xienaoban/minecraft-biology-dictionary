package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class EntityDescriptionWidget extends EntityPropertyWidget<Entity> {
    public static final Factory<Entity> FACTORY = properties -> {
        Component desc = resolveDescription(properties.entity());
        if (desc == null) {
            if (ConfigsManager.getClient().isHideEntityDescriptionWidgetIfNotFound()) { return null; }
            else desc = TextUtils.translate(Lang.TEXT_NO_ENTITY_DESCRIPTION);
        }
        return new EntityDescriptionWidget(properties, desc);
    };

    private static final float TEXT_SCALE = 0.5F;
    private static final float H_PADDING = 0F;
    private static final float V_PADDING = 0F;
    private static final float LINE_SPACING = 1F;

    private static final int COLUMNS = Page.COLUMNS;
    private static final int MAX_VISIBLE_LINES = 2;
    private static final FormattedCharSequence ELLIPSIS = Component.literal("... ...").getVisualOrderText();

    private final Component fullDescription;
    private final List<FormattedCharSequence> lines;

    private EntityDescriptionWidget(EntityProperties<Entity> properties, Component description) {
        super(properties, 1, COLUMNS);
        this.fullDescription = description;
        Font font = TextUtils.getGlobalFont();
        int maxTextWidth = (int)((Widget.calcWidth(COLUMNS) - H_PADDING * 2) / TEXT_SCALE);
        this.lines = TextUtils.toLines(fullDescription, font, maxTextWidth);
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        float lineHeight = TextUtils.getLineHeight(ctx.getFont(), TEXT_SCALE);
        int displayCount = Math.min(lines.size(), MAX_VISIBLE_LINES);
        float availableHeight = getBox().getHeight() - 2 * V_PADDING;

        // Center the first MAX_VISIBLE_LINES lines vertically
        float y = getBox().getTop() + V_PADDING + (availableHeight - displayCount * lineHeight) / 2F;
        float x = getBox().getLeft() + H_PADDING;
        int color = Colors.COMMON_DARK_LIGHTER_TEXT;
        float z = ctx.getZ();

        for (int i = 0; i < displayCount; i++) {
            ctx.renderText(lines.get(i), color, TEXT_SCALE, z, x, y);
            y += lineHeight + LINE_SPACING;
        }

        // "..." on the line right after the visible lines (not centered)
        if (lines.size() > MAX_VISIBLE_LINES) {
            ctx.renderText(ELLIPSIS, color, TEXT_SCALE, z, x, y);
        }
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        float lineHeight = TextUtils.getLineHeight(ctx.getFont(), TEXT_SCALE);
        int displayCount = Math.min(lines.size(), MAX_VISIBLE_LINES);
        float availableHeight = getBox().getHeight() - 2 * V_PADDING;
        float y = getBox().getTop() + V_PADDING + (availableHeight - displayCount * lineHeight) / 2F;
        ctx.renderLinedTooltipCentered(lines, TEXT_SCALE, (getBox().getLeft() + getBox().getRight()) / 2, y - 2);
        return true;
    }

    private static Component resolveDescription(Entity entity) {
        Identifier id = EntityUtils.getEntityTypeId(entity);
        String ns = id.getNamespace();
        String path = id.getPath();

        String[] keys = {
            "entity." + ns + "." + path + ".description",
            "entity." + ns + "." + path + ".desc",
            "lore." + ns + "." + path
        };

        for (String key : keys) {
            if (TextUtils.hasTranslation(key)) {
                return Component.translatable(key);
            }
        }

        return null;
    }
}
